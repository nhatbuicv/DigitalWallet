package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.EntryType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionHistoryResponse {
    private String referenceCode ;
    private EntryType entryType ;
    private String description ;
    private Long amount ;
    private Long balanceAfter;
    private Instant transactionTime;
}
