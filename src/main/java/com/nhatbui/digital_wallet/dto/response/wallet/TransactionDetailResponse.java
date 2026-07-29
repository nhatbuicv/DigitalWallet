package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionDetailResponse {
    private String referenceNumber;
    private String transactionCode;
    private TransactionType transactionType;
    private TransactionStatus status;

    private String fromWallet;

    private String fromOwnerName;

    private String toWallet;
    private String toOwnerName;

    private Long amount;

    private String description;

    private Instant transactionTime;

    private Long balanceAfter;
}
