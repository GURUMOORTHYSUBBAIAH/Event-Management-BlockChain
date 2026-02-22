package com.eventchain.payment;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.entity.Application;
import com.eventchain.entity.Event;
import com.eventchain.entity.Payment;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeService {
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!"SELECTED".equals(application.getStatus())) {
            throw new BadRequestException("Only selected applicants can pay");
        }

        Event event = application.getEvent();
        BigDecimal price = event.getPrice();

        var existingPayment = paymentRepository.findByApplicationId(applicationId);
        if (existingPayment.isPresent() && "COMPLETED".equals(existingPayment.get().getStatus())) {
            throw new BadRequestException("Payment already completed");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&application_id=" + applicationId)
                .setCancelUrl(cancelUrl + "?application_id=" + applicationId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmountDecimal(price.multiply(BigDecimal.valueOf(100)))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Event Ticket: " + event.getTitle())
                                        .setDescription(event.getDescription() != null ? event.getDescription() : "")
                                        .build())
                                .build())
                        .build())
                .putMetadata("application_id", applicationId.toString())
                .putMetadata("event_id", event.getId().toString())
                .putMetadata("user_id", application.getUser().getId().toString())
                .build();

        try {
            com.stripe.Stripe.apiKey = stripeApiKey;
            Session session = Session.create(params);

            Payment payment = existingPayment.orElseGet(Payment::new);
            payment.setApplication(application);
            payment.setStripeSessionId(session.getId());
            payment.setAmount(price);
            payment.setCurrency("USD");
            payment.setStatus("PENDING");
            paymentRepository.save(payment);

            return CheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .url(session.getUrl())
                    .build();
        } catch (Exception e) {
            throw new BadRequestException("Failed to create checkout session: " + e.getMessage());
        }
    }

    public void handleWebhook(String payload, String signature) {
        try {
            com.stripe.Stripe.apiKey = stripeApiKey;
            com.stripe.model.Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                handleCheckoutCompleted(session);
            }
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid webhook signature");
        } catch (Exception e) {
            throw new BadRequestException("Webhook processing failed: " + e.getMessage());
        }
    }

    private void handleCheckoutCompleted(Session session) {
        var paymentOpt = paymentRepository.findByStripeSessionId(session.getId());
        if (paymentOpt.isEmpty()) return;
        Payment payment = paymentOpt.get();
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        var application = payment.getApplication();
        application.setStatus("PAID");
        applicationRepository.save(application);
    }
}
