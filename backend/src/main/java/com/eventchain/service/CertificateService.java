package com.eventchain.service;

import com.eventchain.entity.Certificate;
import com.eventchain.entity.Ticket;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.CertificateRepository;
import com.eventchain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final TicketRepository ticketRepository;

    @Autowired(required = false)
    private com.eventchain.blockchain.NftContractService nftContractService;

    @Transactional
    public byte[] generateCertificate(Long ticketId, org.springframework.security.core.Authentication auth)
            throws Exception {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        if (!ticket.getCheckedIn()) {
            throw new IllegalArgumentException("Attendance must be marked before certificate");
        }

        var existingCert = certificateRepository.findByTicketId(ticketId);
        if (existingCert.isPresent()) {
            return generatePdfContent(existingCert.get());
        }

        String certificateId = "CERT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String verificationUrl = "https://eventchain.io/verify/" + certificateId;

        byte[] pdfContent = generatePdf(ticket, certificateId, verificationUrl);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(pdfContent);

        String txHash = null;
        if (nftContractService != null) {
            try {
                nftContractService.issueCertificateHash(ticket.getTokenId(), hash);
                txHash = "0x";
            } catch (Exception e) {
                // Continue without blockchain
            }
        }

        Certificate cert = new Certificate();
        cert.setTicket(ticket);
        cert.setUser(ticket.getUser());
        cert.setEvent(ticket.getEvent());
        cert.setCertificateId(certificateId);
        cert.setFileHash(bytesToHex(hash));
        cert.setTransactionHash(txHash);
        certificateRepository.save(cert);

        return pdfContent;
    }

    private byte[] generatePdfContent(Certificate cert) throws Exception {
        return generatePdf(
                cert.getTicket(),
                cert.getCertificateId(),
                "https://eventchain.io/verify/" + cert.getCertificateId());
    }

    private byte[] generatePdf(Ticket ticket, String certificateId, String verificationUrl) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(new Paragraph("CERTIFICATE OF ATTENDANCE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24)));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("This certifies that"));
        document.add(new Paragraph(ticket.getUser().getDisplayName() != null ? ticket.getUser().getDisplayName() : ticket.getUser().getEmail(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("attended"));
        document.add(new Paragraph(ticket.getEvent().getTitle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Certificate ID: " + certificateId));
        document.add(new Paragraph("Verification: " + verificationUrl));
        document.add(new Paragraph("Date: " + LocalDateTime.now().toLocalDate()));

        document.close();
        return baos.toByteArray();
    }

    @Transactional(readOnly = true)
    public boolean verifyCertificate(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId).isPresent();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
