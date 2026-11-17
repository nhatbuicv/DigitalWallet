package com.nhatbui.wallet_ledger.dto.request.wallet;

import com.nhatbui.wallet_ledger.enums.EntryType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryRequest {
    private EntryType entryType;
    private Instant fromDate;
    private Instant toDate;
}