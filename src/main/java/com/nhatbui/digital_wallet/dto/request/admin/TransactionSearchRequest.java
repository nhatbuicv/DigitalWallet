package com.nhatbui.digital_wallet.dto.request.admin;

import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
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