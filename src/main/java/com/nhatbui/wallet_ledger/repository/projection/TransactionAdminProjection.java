package com.nhatbui.wallet_ledger.repository.projection;

import com.nhatbui.wallet_ledger.enums.CurrencyCode;
import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.TransactionType;

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
