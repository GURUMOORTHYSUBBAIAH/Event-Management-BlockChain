package com.eventchain.controller;

import com.eventchain.payment.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {
    private final StripeService stripeService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        stripeService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
