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
