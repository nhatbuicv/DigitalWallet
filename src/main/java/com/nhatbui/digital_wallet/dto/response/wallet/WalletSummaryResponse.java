package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
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
