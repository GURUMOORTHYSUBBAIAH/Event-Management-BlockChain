package com.eventchain.service;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.payment.StripeService;
import com.eventchain.entity.Payment;
import com.eventchain.entity.Ticket;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.PaymentRepository;
import com.eventchain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketRepository ticketRepository;

    @Autowired(required = false)
    private com.eventchain.blockchain.NftContractService nftContractService;

    public CheckoutSessionResponse createCheckoutSession(Long applicationId) {
        return stripeService.createCheckoutSession(applicationId);
    }

    @Transactional
    public void handlePaymentSuccess(String sessionId) {
        var paymentOpt = paymentRepository.findByStripeSessionId(sessionId);
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
