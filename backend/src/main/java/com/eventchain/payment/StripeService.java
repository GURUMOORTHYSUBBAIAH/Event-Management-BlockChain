package com.eventchain.payment;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeService {
    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.success-url:}")
    private String successUrl;

    @Value("${stripe.cancel-url:}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(Long applicationId) {
        throw new BadRequestException("Stripe is not configured in this build");
    }

    public void handleWebhook(String payload, String signature) {
        // no-op
    }
}
