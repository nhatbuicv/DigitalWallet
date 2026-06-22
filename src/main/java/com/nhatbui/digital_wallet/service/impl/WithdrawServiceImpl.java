package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.TransferMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.SystemWalletService;
import com.nhatbui.digital_wallet.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;
    private final SystemWalletService systemWalletService;
    private final WalletRepository walletRepository;
    private final SecurityUtils securityUtils;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    @Override
    public ApiResponse<TransferResponse> withdraw(WithdrawRequest withdrawRequest) {

        var existing = transactionRepository.findByIdempotencyKey(withdrawRequest.getIdempotencyKey());

        if (existing.isPresent()) {
            return ApiResponse.<TransferResponse>builder()
                    .success(true)
                    .message("Rút tiền thành công")
                    .data(transferMapper.mapToTransferResponse(existing.get()))
                    .build();
        }

        if (withdrawRequest.getAmount() < 1000L) {
            throw new AppException(ErrorCode.INVALID_WITHDRAW_AMOUNT);
        }

        UUID userId = securityUtils.getCurrentUserId();

        Wallet systemWallet = systemWalletService.getSystemWallet();

        Wallet userWallet = walletRepository.findByWalletNumberAndUserUid(withdrawRequest.getWalletNumber(),userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        List<Wallet> locked = lockInOrder(systemWallet, userWallet);
        Wallet lockedSystem = locked.get(0).getWalletNumber().equals(systemWallet.getWalletNumber()) ? locked.get(0) : locked.get(1);

        Wallet lockedUser = locked.get(0).getWalletNumber().equals(userWallet.getWalletNumber()) ? locked.get(0) : locked.get(1);

        if (lockedUser.getStatus() != WalletStatus.ACTIVE) {
            throw new AppException(ErrorCode.WALLET_NOT_ACTIVE);
        }

        if (lockedUser.getBalance() < withdrawRequest.getAmount()) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .idempotencyKey(withdrawRequest.getIdempotencyKey())
                .fromWallet(lockedUser)
                .toWallet(lockedSystem)
                .amount(withdrawRequest.getAmount())
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.PROCESSING)
                .description(withdrawRequest.getDescription())
                .createdAt(now)
                .build();

        transactionRepository.save(transaction);

        Long userBalanceAfter   = lockedUser.getBalance()   - withdrawRequest.getAmount();
        Long systemBalanceAfter = lockedSystem.getBalance() + withdrawRequest.getAmount();

        LedgerEntry debitUser = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedUser)
                .entryType(EntryType.DEBIT)
                .amount(withdrawRequest.getAmount())
                .balanceAfter(userBalanceAfter)
                .createdAt(now)
                .build();

        LedgerEntry creditSystem = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedSystem)
                .entryType(EntryType.CREDIT)
                .amount(withdrawRequest.getAmount())
                .balanceAfter(systemBalanceAfter)
                .createdAt(now)
                .build();

        ledgerEntryRepository.saveAll(List.of(debitUser, creditSystem));

        lockedUser.setBalance(userBalanceAfter);
        lockedSystem.setBalance(systemBalanceAfter);

        walletRepository.saveAll(List.of(lockedUser, lockedSystem));

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());

        transactionRepository.save(transaction);

        return ApiResponse.<TransferResponse>builder()
                .success(true)
                .message("Rút tiền thành công")
                .data(transferMapper.mapToTransferResponse(transaction))
                .build();
    }


    private List<Wallet> lockInOrder(Wallet a, Wallet b) {
        boolean aFirst = a.getUid().compareTo(b.getUid()) < 0;
        Wallet first  = aFirst ? a : b;
        Wallet second = aFirst ? b : a;

        Wallet lockedFirst = walletRepository.findByWalletNumberWithLock(first.getWalletNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Wallet lockedSecond = walletRepository.findByWalletNumberWithLock(second.getWalletNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        return List.of(lockedFirst, lockedSecond);
    }
}
