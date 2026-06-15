package com.nhatbui.digital_wallet.dto.request;

import com.nhatbui.digital_wallet.enums.WalletType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateWalletRequest {
    @NotNull
    private WalletType walletType;
}
