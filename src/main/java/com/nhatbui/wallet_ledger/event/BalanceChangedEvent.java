package com.nhatbui.wallet_ledger.event;

import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record BalanceChangedEvent(
        UUID eventId,
        UUID userId,
        String maskedWalletNumber,
        EntryType entryType,
        TransactionType transactionType,
        Long amount,
        Long balanceAfter,
        Long availableBalance,
        Instant occurredAt) {
}
