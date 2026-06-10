package com.nhatbui.digital_bank.dto.response;

import com.nhatbui.digital_bank.enums.TransactionStatus;
import com.nhatbui.digital_bank.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private String transactionId;
    private String referenceCode;

    private String fromAccountId;
    private String toAccountId;

    private BigDecimal amount;
    private String currency;

    private TransactionType type;
    private TransactionStatus status;

    private String description;
    private Instant createdAt;
    private Instant completedAt;
}