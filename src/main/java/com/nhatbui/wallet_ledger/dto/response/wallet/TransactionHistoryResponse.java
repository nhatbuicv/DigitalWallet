package com.nhatbui.wallet_ledger.dto.response.wallet;

import com.nhatbui.wallet_ledger.enums.EntryType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionHistoryResponse {
    private String transactionId;
    private String referenceCode;
    private EntryType entryType;
    private String description;
    private Long amount;
    private Long balanceAfter;
    private Instant createdAt;
}
