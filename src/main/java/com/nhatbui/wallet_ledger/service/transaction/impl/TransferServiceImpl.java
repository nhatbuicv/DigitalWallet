package com.nhatbui.wallet_ledger.service.transaction.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhatbui.wallet_ledger.config.SecurityUtils;
import com.nhatbui.wallet_ledger.dto.request.wallet.TransferRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.IdempotentResult;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;
import com.nhatbui.wallet_ledger.entity.LedgerEntry;
import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.*;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.mapper.TransferMapper;
import com.nhatbui.wallet_ledger.repository.LedgerEntryRepository;
import com.nhatbui.wallet_ledger.repository.TransactionRepository;
import com.nhatbui.wallet_ledger.repository.WalletRepository;
import com.nhatbui.wallet_ledger.service.idempotency.IdempotentExecutor;
import com.nhatbui.wallet_ledger.service.idempotency.RequestHasher;
import com.nhatbui.wallet_ledger.service.transaction.TransferService;
import lombok.RequiredArgsConstructor;
import com.nhatbui.wallet_ledger.service.notification.BalanceChangeNotifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private static final TypeReference<ApiResponse<TransferResponse>> RESPONSE_TYPE = new TypeReference<>() {};
    private final SecurityUtils securityUtils;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferMapper transferMapper;
    private final TransactionFactory transactionFactory;
    private final IdempotentExecutor idempotentExecutor;
    private final RequestHasher requestHasher;
    private final BalanceChangeNotifier balanceChangeNotifier;

    @Override
    @PreAuthorize("isAuthenticated()")
    public IdempotentResult<ApiResponse<TransferResponse>> transfer(TransferRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        String hash = requestHasher.hash(
                IdempotencyOperation.TRANSFER,
                request.getFromWalletNumber(),
                request.getToWalletNumber(),
                request.getAmount(),
                request.getDescription());

        return idempotentExecutor.execute(
                userId,
                IdempotencyOperation.TRANSFER,
                request.getIdempotencyKey(),
                hash,
                RESPONSE_TYPE,
                () -> doTransfer(request, userId));
    }

    private ApiResponse<TransferResponse> doTransfer(TransferRequest request, UUID userId) {
        if (request.getFromWalletNumber().equals(request.getToWalletNumber()))
            throw new AppException(ErrorCode.TRANSFER_SAME_WALLET);

        Wallet fromWallet = walletRepository.findByWalletNumberAndUserUid(request.getFromWalletNumber(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet toWallet = walletRepository.findByWalletNumber(request.getToWalletNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        UUID fromUid = fromWallet.getUid();
        UUID toUid   = toWallet.getUid();

        Wallet lockedFirst  = fromUid.compareTo(toUid) < 0
                ? walletRepository.findByUidWithLock(fromUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND))
                : walletRepository.findByUidWithLock(toUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet lockedSecond = fromUid.compareTo(toUid) < 0
                ? walletRepository.findByUidWithLock(toUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND))
                : walletRepository.findByUidWithLock(fromUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        fromWallet = lockedFirst.getUid().equals(fromUid) ? lockedFirst : lockedSecond;
        toWallet   = lockedFirst.getUid().equals(toUid)   ? lockedFirst : lockedSecond;

        if (fromWallet.getStatus() != WalletStatus.ACTIVE)
            throw new AppException(ErrorCode.fromWalletStatus(fromWallet.getStatus()));

        if (toWallet.getStatus() != WalletStatus.ACTIVE)
            throw new AppException(ErrorCode.fromWalletStatus(toWallet.getStatus()));

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);

        Transaction transaction = transactionFactory.createTransfer(
                userId, fromWallet, toWallet, request.getAmount(),
                request.getIdempotencyKey(), generateDescription(fromWallet, request));

        transactionRepository.save(transaction);

        var fromBalanceBefore = fromWallet.getBalance();
        var fromBalanceAfter  = fromBalanceBefore - request.getAmount();
        fromWallet.setBalance(fromBalanceAfter);
        fromWallet.setAvailableBalance(fromBalanceAfter);
        walletRepository.save(fromWallet);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transaction(transaction)
                .wallet(fromWallet)
                .entryType(EntryType.DEBIT)
                .amount(request.getAmount())
                .balanceBefore(fromBalanceBefore)
                .balanceAfter(fromBalanceAfter)
                .createdAt(Instant.now())
                .build());

        var toBalanceBefore = toWallet.getBalance();
        var toBalanceAfter  = toBalanceBefore + request.getAmount();
        toWallet.setBalance(toBalanceAfter);
        toWallet.setAvailableBalance(toBalanceAfter);
        walletRepository.save(toWallet);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transaction(transaction)
                .wallet(toWallet)
                .entryType(EntryType.CREDIT)
                .amount(request.getAmount())
                .balanceBefore(toBalanceBefore)
                .balanceAfter(toBalanceAfter)
                .createdAt(Instant.now())
                .build());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());
        transactionRepository.save(transaction);

        balanceChangeNotifier.notifyBalanceChanged(
                fromWallet, EntryType.DEBIT, TransactionType.TRANSFER, request.getAmount());
        balanceChangeNotifier.notifyBalanceChanged(
                toWallet, EntryType.CREDIT, TransactionType.TRANSFER, request.getAmount());

        return ApiResponse.<TransferResponse>builder()
                .success(true)
                .message("Chuyển tiền thành công")
                .data(transferMapper.mapToTransferResponse(transaction))
                .build();
    }

    private String generateDescription(Wallet fromWallet, TransferRequest request) {
        if (request.getDescription() != null && !request.getDescription().isBlank())
            return request.getDescription();
        return fromWallet.getWalletName() + " chuyển tiền";
    }
}
