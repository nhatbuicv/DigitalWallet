package com.nhatbui.wallet_ledger.dto.request.wallet;

import com.nhatbui.wallet_ledger.enums.WalletType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateWalletRequest {
    @NotNull
    private WalletType walletType;
}
