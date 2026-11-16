package com.nhatbui.digital_wallet.repository.projection;

import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record TransactionAdminProjection(
        UUID id,
        String referenceCode,
        TransactionType type,
        TransactionStatus status,
        CurrencyCode currency,
        Long amount,
        String description,
        UUID fromWalletId,
        String fromWalletNumber,
        String fromOwnerName,
        UUID toWalletId,
        String toWalletNumber,
        String toOwnerName,
        Instant createdAt,
        Instant completedAt
) {
}
