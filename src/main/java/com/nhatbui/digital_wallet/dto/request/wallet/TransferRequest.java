package com.nhatbui.digital_wallet.dto.request.wallet;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
public class TransferRequest {

    @NotBlank(message = "From Wallet is required")
    private String fromWalletNumber;

    @NotBlank(message = "To Wallet is required")
    private String toWalletNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum transfer amount is 1,000")
    @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
    private Long amount;

    @NotNull
    private String description;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 64, message = "Idempotency key must not exceed 64 characters")
    private String idempotencyKey;
}