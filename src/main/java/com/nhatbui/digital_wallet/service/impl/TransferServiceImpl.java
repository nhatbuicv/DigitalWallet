package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.*;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.TransferMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final SecurityUtils securityUtils;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TransferResponse> transfer(TransferRequest request) {

        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return ApiResponse.<TransferResponse>builder()
                    .success(true)
                    .message("Chuyển tiền thành công")
                    .data(transferMapper.mapToTransferResponse(existing.get()))
                    .build();
        }

        if (request.getFromWalletNumber().equals(request.getToWalletNumber()))
            throw new AppException(ErrorCode.TRANSFER_SAME_WALLET);

        User user = securityUtils.getCurrentUser();

        Wallet fromWallet = walletRepository.findByWalletNumberAndUserUid(request.getFromWalletNumber(), user.getUid())
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
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);

        if (toWallet.getStatus() != WalletStatus.ACTIVE)
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);

        Transaction transaction = Transaction.builder()
                .referenceCode(generateReferenceCode())
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(request.getAmount())
                .currency(CurrencyCode.VND)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(generateDescription(fromWallet, request))
                .idempotencyKey(request.getIdempotencyKey())
                .build();

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

        return ApiResponse.<TransferResponse>builder()
                .success(true)
                .message("Chuyển tiền thành công")
                .data(transferMapper.mapToTransferResponse(transaction))
                .build();
    }

    private String generateReferenceCode() {
        return "TXN" + Instant.now().toEpochMilli()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateDescription(Wallet fromWallet, TransferRequest request) {
        if (request.getDescription() != null && !request.getDescription().isBlank())
            return request.getDescription();
        return fromWallet.getWalletName() + " chuyển tiền";
    }
}