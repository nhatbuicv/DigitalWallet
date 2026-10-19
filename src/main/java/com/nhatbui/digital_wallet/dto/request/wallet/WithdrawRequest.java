package com.nhatbui.digital_wallet.dto.request.wallet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawRequest {
    @NotBlank(message = "Wallet number is required")
    private String walletNumber;

    @NotNull(message = "Amount is required")
    @Min(value = 1000, message = "Minimum deposit amount is 1,000 VND")
    private Long amount;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 64, message = "Idempotency key must not exceed 64 characters")
    private String idempotencyKey;

    private String description;            
}
