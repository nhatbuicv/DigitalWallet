package com.nhatbui.digital_wallet.dto.response.account;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WalletResponse {
    private UUID uid;
    private String accountNumber;
    private String accountName;
    private WalletType walletType;

    private CurrencyCode currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal blockedBalance;

    private WalletStatus status;
    private String branchCode;
    private String branchName;

    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;

    private Instant openedAt;
    private Instant createdAt;

}