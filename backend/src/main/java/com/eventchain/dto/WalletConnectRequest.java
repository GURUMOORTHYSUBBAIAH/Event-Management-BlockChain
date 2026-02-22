package com.eventchain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WalletConnectRequest {
    @NotBlank(message = "Wallet address is required")
    private String walletAddress;

    @NotBlank(message = "Signature is required")
    private String signature;

    @NotBlank(message = "Message is required")
    private String message;
}
