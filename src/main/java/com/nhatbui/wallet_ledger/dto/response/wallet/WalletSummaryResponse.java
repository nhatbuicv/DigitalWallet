package com.nhatbui.wallet_ledger.dto.response.wallet;

import com.nhatbui.wallet_ledger.enums.WalletStatus;
import com.nhatbui.wallet_ledger.enums.WalletType;
import com.nhatbui.wallet_ledger.enums.CurrencyCode;
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
