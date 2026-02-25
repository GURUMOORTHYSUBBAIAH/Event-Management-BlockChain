package com.eventchain.service;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.entity.Payment;
import com.eventchain.entity.Application;
import com.eventchain.entity.Ticket;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.PaymentRepository;
import com.eventchain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceSimple {
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(Long applicationId) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        
        // For now, return a simple session response
        return CheckoutSessionResponse.builder()
                .sessionId("session_" + applicationId)
                .url("http://localhost:5173/payment/success")
                .build();
    }

    @Transactional
    public void handleWebhook(String orderId, String paymentId, String signature) {
        // Simple webhook handler - mark payment as completed
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
            ticket.setTokenId(System.currentTimeMillis());
            ticketRepository.save(ticket);
        }
    }

    public Optional<Payment> getPaymentByApplicationId(Long applicationId) {
        return paymentRepository.findByApplicationId(applicationId);
    }
}
