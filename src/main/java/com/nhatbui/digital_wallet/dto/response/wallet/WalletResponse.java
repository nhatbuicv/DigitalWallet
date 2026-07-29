package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class WalletResponse {
    private UUID uid;
    private String walletNumber;
    private String walletName;
    private WalletType walletType;

    private CurrencyCode currency;
    private Long balance;
    private Long availableBalance;
    private Long blockedBalance;

    private WalletStatus status;
    private String branchCode;
    private String branchName;

    private Long dailyLimit;
    private Long monthlyLimit;

    private Instant openedAt;
    private Instant createdAt;

}