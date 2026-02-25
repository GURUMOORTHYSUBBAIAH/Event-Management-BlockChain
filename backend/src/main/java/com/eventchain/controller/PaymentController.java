package com.eventchain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventchain.dto.CheckoutSessionResponse;
import com.eventchain.service.PaymentService;

import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<String> handleSuccess() {
        return ResponseEntity.ok("Payment successful");
    }
}
