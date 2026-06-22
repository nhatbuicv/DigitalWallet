package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WalletSummaryResponse {
    private UUID uid;
    private String walletNumber;
    private String walletName;
    private WalletType walletType;

    private CurrencyCode currency;
    private Long balance;

    private WalletStatus status;
}
