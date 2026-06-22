package com.nhatbui.digital_wallet.dto.response;

import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.Instant;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private String transactionId;
    private String referenceCode;

    private String fromWalletId;
    private String toWalletId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;

    private TransactionType type;
    private TransactionStatus status;

    private String description;
    private Instant createdAt;
    private Instant completedAt;
}