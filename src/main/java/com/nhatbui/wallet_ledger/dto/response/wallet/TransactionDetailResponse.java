package com.nhatbui.wallet_ledger.dto.response.wallet;

import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionDetailResponse {
    private String transactionId;
    private String referenceCode;
    private TransactionType type;
    private TransactionStatus status;
    private String fromWalletNumber;
    private String fromOwnerName;
    private String toWalletNumber;
    private String toOwnerName;
    private Long amount;
    private String description;
    private Instant createdAt;
    private Long balanceAfter;
}
