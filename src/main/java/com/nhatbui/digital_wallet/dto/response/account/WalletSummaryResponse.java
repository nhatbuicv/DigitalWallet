package com.nhatbui.digital_wallet.dto.response.account;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WalletSummaryResponse {
    private UUID uid;
    private String accountNumber;
    private String accountName;
    private WalletType walletType;

    private CurrencyCode currency;
    private BigDecimal balance;

    private WalletStatus status;
}
