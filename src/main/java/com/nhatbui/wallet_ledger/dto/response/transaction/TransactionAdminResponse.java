package com.nhatbui.wallet_ledger.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.wallet_ledger.enums.CurrencyCode;
import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAdminResponse {
    private UUID id;
    private String referenceCode;
    private TransactionType type;
    private TransactionStatus status;
    private CurrencyCode currency;
    private Long amount;
    private String description;
    private UUID fromWalletId;
    private String fromWalletNumber;
    private String fromOwnerName;
    private UUID toWalletId;
    private String toWalletNumber;
    private String toOwnerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant completedAt;
}