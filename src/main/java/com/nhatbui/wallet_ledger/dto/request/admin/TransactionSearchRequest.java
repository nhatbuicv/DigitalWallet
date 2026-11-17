package com.nhatbui.wallet_ledger.dto.request.admin;

import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {
    private String reference;
    private TransactionStatus status;
    private TransactionType type;
    private Instant fromDate;
    private Instant toDate;
}