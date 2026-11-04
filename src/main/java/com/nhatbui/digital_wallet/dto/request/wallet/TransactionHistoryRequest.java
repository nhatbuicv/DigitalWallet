package com.nhatbui.digital_wallet.dto.request.wallet;

import com.nhatbui.digital_wallet.enums.EntryType;
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