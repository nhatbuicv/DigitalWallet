package com.nhatbui.digital_wallet.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAdminResponse {
    private String id;
    private String referenceCode;
    private TransactionType type;
    private TransactionStatus status;
    private CurrencyCode currency;
    private Long amount;
    private String description;
    private String fromWalletId;
    private String fromWalletNumber;
    private String fromOwnerName;
    private String toWalletId;
    private String toWalletNumber;
    private String toOwnerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant completedAt;
}
