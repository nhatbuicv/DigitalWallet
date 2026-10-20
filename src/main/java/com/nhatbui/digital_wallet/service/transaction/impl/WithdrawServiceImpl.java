package com.nhatbui.digital_wallet.service.transaction.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.generator.ReferenceCodeGenerator;
import com.nhatbui.digital_wallet.mapper.TransferMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.transaction.WithdrawService;
import com.nhatbui.digital_wallet.service.wallet.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;
    private final TransactionFactory transactionFactory;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TransferResponse> withdraw(WithdrawRequest withdrawRequest) {
        UUID userId = securityUtils.getCurrentUserId();

        var existing = transactionRepository
                .findByUserIdAndIdempotencyKey(userId, withdrawRequest.getIdempotencyKey());

        if (existing.isPresent()) {
            return successResponse(existing.get());
        }

        try {
            return transactionTemplate.execute(status -> doWithdraw(withdrawRequest, userId));
        } catch (DataIntegrityViolationException | ConcurrencyFailureException e) {
            // Trùng key do request song song -> trả lại giao dịch đã ghi
            var raced = transactionRepository
                    .findByUserIdAndIdempotencyKey(userId, withdrawRequest.getIdempotencyKey());
            if (raced.isPresent()) {
                return successResponse(raced.get());
            }
            throw e;
        }
    }

    private ApiResponse<TransferResponse> doWithdraw(WithdrawRequest withdrawRequest, UUID userId) {
        if (withdrawRequest.getAmount() < 1000L) {
            throw new AppException(ErrorCode.INVALID_WITHDRAW_AMOUNT);
        }

        Wallet systemWallet = systemWalletService.getSystemWallet();

        Wallet userWallet = walletRepository
                .findByWalletNumberAndUserUid(withdrawRequest.getWalletNumber(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        List<Wallet> locked = lockInOrder(systemWallet, userWallet);
        Wallet lockedSystem = locked.get(0).getWalletNumber().equals(systemWallet.getWalletNumber()) ? locked.get(0) : locked.get(1);

        Wallet lockedUser = locked.get(0).getWalletNumber().equals(userWallet.getWalletNumber()) ? locked.get(0) : locked.get(1);

        if (lockedUser.getStatus() != WalletStatus.ACTIVE) {
            throw new AppException(ErrorCode.fromWalletStatus(lockedUser.getStatus()));
        }

        if (lockedUser.getBalance() < withdrawRequest.getAmount()) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Instant now = Instant.now();

        Transaction transaction = transactionFactory.createWithdraw(
                userId, lockedUser, lockedSystem, withdrawRequest.getAmount(),
                withdrawRequest.getIdempotencyKey(), withdrawRequest.getDescription());

        transactionRepository.save(transaction);

        Long userBalanceBefore = lockedUser.getBalance();
        Long systemBalanceBefore = lockedSystem.getBalance();
        Long userBalanceAfter = userBalanceBefore - withdrawRequest.getAmount();
        Long systemBalanceAfter = systemBalanceBefore + withdrawRequest.getAmount();

        LedgerEntry debitUser = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedUser)
                .entryType(EntryType.DEBIT)
                .amount(withdrawRequest.getAmount())
                .balanceBefore(userBalanceBefore)
                .balanceAfter(userBalanceAfter)
                .createdAt(now)
                .build();

        LedgerEntry creditSystem = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedSystem)
                .entryType(EntryType.CREDIT)
                .amount(withdrawRequest.getAmount())
                .balanceBefore(systemBalanceBefore)
                .balanceAfter(systemBalanceAfter)
                .createdAt(now)
                .build();

        ledgerEntryRepository.saveAll(List.of(debitUser, creditSystem));

        lockedUser.setBalance(userBalanceAfter);
        lockedUser.setAvailableBalance(userBalanceAfter);

        lockedSystem.setBalance(systemBalanceAfter);
        lockedSystem.setAvailableBalance(systemBalanceAfter);

        walletRepository.saveAll(List.of(lockedUser, lockedSystem));

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());

        transactionRepository.save(transaction);

        return successResponse(transaction);
    }

    private ApiResponse<TransferResponse> successResponse(Transaction transaction) {
        return ApiResponse.<TransferResponse>builder()
                .success(true)
                .message("Rút tiền thành công")
                .data(transferMapper.mapToTransferResponse(transaction))
                .build();
    }

    private List<Wallet> lockInOrder(Wallet a, Wallet b) {
        boolean aFirst = a.getUid().compareTo(b.getUid()) < 0;
        Wallet first = aFirst ? a : b;
        Wallet second = aFirst ? b : a;

        Wallet lockedFirst = walletRepository.findByWalletNumberWithLock(first.getWalletNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Wallet lockedSecond = walletRepository.findByWalletNumberWithLock(second.getWalletNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        return List.of(lockedFirst, lockedSecond);
    }
}
