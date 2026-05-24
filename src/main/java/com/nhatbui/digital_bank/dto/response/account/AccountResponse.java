package com.nhatbui.digital_bank.dto.response.account;

import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.CurrencyCode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID uid;
    private String accountNumber;
    private String accountName;
    private AccountType accountType;

    private CurrencyCode currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal blockedBalance;

    private AccountStatus status;
    private String branchCode;
    private String branchName;

    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;

    private Instant openedAt;
    private Instant createdAt;

}