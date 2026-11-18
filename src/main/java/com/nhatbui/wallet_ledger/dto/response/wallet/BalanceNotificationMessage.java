package com.nhatbui.wallet_ledger.dto.response.wallet;

import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.TransactionType;

import java.time.Instant;
import java.util.UUID;

/** JSON pushed to the mobile client over WebSocket. Intentionally minimal (no description / counterparty). */
public record BalanceNotificationMessage(
        UUID eventId,
        String type,
        String walletNumber,
        EntryType entryType,
        TransactionType transactionType,
        Long amount,
        Long balanceAfter,
        Long availableBalance,
        Instant occurredAt) {

    public static final String BALANCE_CHANGED = "BALANCE_CHANGED";
}
