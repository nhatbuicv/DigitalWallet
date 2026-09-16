package com.nhatbui.digital_wallet.dto.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WalletStatusRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}
