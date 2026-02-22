package com.eventchain.controller;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/checkout/{applicationId}")
    public ResponseEntity<CheckoutSessionResponse> createCheckout(@PathVariable Long applicationId) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(applicationId));
    }

    @PostMapping("/success")
    public ResponseEntity<Void> paymentSuccess(@RequestParam String session_id) {
        paymentService.handlePaymentSuccess(session_id);
        return ResponseEntity.ok().build();
    }
}
