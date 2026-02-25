package com.eventchain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.entity.Payment;
import com.eventchain.entity.Ticket;
import com.eventchain.exception.NotFoundException;
import com.eventchain.payment.RazorpayService;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.PaymentRepository;
import com.eventchain.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final RazorpayService razorpayService;
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketRepository ticketRepository;

    @Autowired(required = false)
    private com.eventchain.blockchain.NftContractService nftContractService;

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(Long applicationId) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        
        String orderId = razorpayService.createCheckoutSession(
            applicationId, 
            application.getEvent().getTitle(), 
            application.getUser().getEmail(), 
            application.getEvent().getPrice()
        );
        
        var payment = new Payment();
        payment.setApplication(application);
        payment.setAmount(application.getEvent().getPrice());
        payment.setCurrency("INR");
        payment.setStatus("PENDING");
        payment.setRazorpayOrderId(orderId);
        payment = paymentRepository.save(payment);
        
        CheckoutSessionResponse resp = new CheckoutSessionResponse();
        resp.setSessionId(orderId);
        resp.setUrl("https://razorpay.com/pay/" + orderId);
        return resp;
    }

    @Transactional
    public void handlePaymentSuccess(String sessionId) {
        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(sessionId);
        if (paymentOpt.isEmpty()) return;

        Payment payment = paymentOpt.get();
        if ("COMPLETED".equals(payment.getStatus())) {
            mintAndCreateTicketIfNeeded(payment);
            return;
        }

        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        var app = payment.getApplication();
        app.setStatus("PAID");
        applicationRepository.save(app);

        mintAndCreateTicketIfNeeded(payment);
    }

    @Transactional
    public void handleWebhook(String orderId, String paymentId, String signature) {
        if (razorpayService.verifyPayment(orderId, paymentId, signature)) {
            var payment = paymentRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new NotFoundException("Payment not found"));
            
            payment.setStatus("COMPLETED");
            paymentRepository.save(payment);
            
            // Create ticket if payment is successful
            var application = payment.getApplication();
            if (application != null && "SELECTED".equals(application.getStatus())) {
                var ticket = new Ticket();
                ticket.setApplication(application);
                ticket.setEvent(application.getEvent());
                ticket.setUser(application.getUser());
                ticket.setTokenId(System.currentTimeMillis()); // Temporary token ID
                ticketRepository.save(ticket);
                
                // Mint NFT if blockchain is enabled
                try {
                    nftContractService.mintTicket(
                        application.getUser().getWalletAddress(),
                        application.getEvent().getId(),
                        "https://metadata.example.com/ticket/" + ticket.getId()
                    );
                } catch (Exception e) {
                    // Log error but don't fail the payment
                    System.err.println("Failed to mint NFT: " + e.getMessage());
                }
            }
        }
    }

    public Optional<Payment> getPaymentByApplicationId(Long applicationId) {
        return paymentRepository.findByApplicationId(applicationId);
    }

    private void mintAndCreateTicketIfNeeded(Payment payment) {
        var application = payment.getApplication();
        boolean hasTicket = ticketRepository.findByUserId(application.getUser().getId()).stream()
                .anyMatch(t -> t.getEvent().getId().equals(application.getEvent().getId()));
        if (hasTicket) return;

        if (nftContractService != null && payment.getTransactionHash() == null) {
            try {
                String walletAddress = application.getUser().getWalletAddress();
                if (walletAddress == null || walletAddress.isBlank()) {
                    walletAddress = "0x0000000000000000000000000000000000000000";
                }
                var result = nftContractService.mintTicket(
                        walletAddress,
                        application.getEvent().getId(),
                        "https://eventchain.io/ticket/" + application.getId());

                Ticket ticket = new Ticket();
                ticket.setApplication(application);
                ticket.setEvent(application.getEvent());
                ticket.setUser(application.getUser());
                ticket.setTokenId(result.tokenId());
                ticket.setTransactionHash(result.transactionHash());
                ticketRepository.save(ticket);

                payment.setTransactionHash(result.transactionHash());
                paymentRepository.save(payment);
            } catch (Exception e) {
                // Log and continue - ticket creation can be retried
            }
        }
    }
}
