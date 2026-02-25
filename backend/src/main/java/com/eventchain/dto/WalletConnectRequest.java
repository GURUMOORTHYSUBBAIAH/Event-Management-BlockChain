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

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
