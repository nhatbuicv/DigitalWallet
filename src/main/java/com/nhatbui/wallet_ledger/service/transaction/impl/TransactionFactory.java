package com.nhatbui.wallet_ledger.service.transaction.impl;

import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.CurrencyCode;
import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import com.nhatbui.wallet_ledger.generator.ReferenceCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionFactory {
    private final ReferenceCodeGenerator referenceCodeGenerator;

    public Transaction createTransfer(UUID userId, Wallet fromWallet, Wallet toWallet, long amount, String idempotencyKey, String description) {
        return build(userId,
                fromWallet,
                toWallet,
                amount,
                CurrencyCode.VND,
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                idempotencyKey,
                description);
    }

    public Transaction createDeposit(UUID userId, Wallet fromWallet, Wallet toWallet, long amount, String idempotencyKey, String description) {
        return build(userId,
                fromWallet,
                toWallet,
                amount,
                CurrencyCode.VND,
                TransactionType.DEPOSIT,
                TransactionStatus.PROCESSING,
                idempotencyKey,
                description);
    }

    public Transaction createWithdraw(UUID userId, Wallet fromWallet, Wallet toWallet, long amount, String idempotencyKey, String description) {
        return build(userId,
                fromWallet,
                toWallet,
                amount,
                CurrencyCode.VND,
                TransactionType.WITHDRAW,
                TransactionStatus.PROCESSING,
                idempotencyKey,
                description);
    }

    private Transaction build(UUID userId, Wallet from, Wallet to, long amount, CurrencyCode currency, TransactionType type, TransactionStatus initialStatus, String idempotencyKey, String description) {
        return Transaction.builder()
                .referenceCode(referenceCodeGenerator.generateReferenceCode())
                .userId(userId)
                .fromWallet(from)
                .toWallet(to)
                .amount(amount)
                .currency(currency)
                .type(type)
                .status(initialStatus)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();
    }
}
