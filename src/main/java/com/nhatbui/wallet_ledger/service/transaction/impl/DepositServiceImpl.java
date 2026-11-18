package com.nhatbui.wallet_ledger.service.transaction.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhatbui.wallet_ledger.config.SecurityUtils;
import com.nhatbui.wallet_ledger.dto.request.wallet.DepositRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.IdempotentResult;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;
import com.nhatbui.wallet_ledger.entity.LedgerEntry;
import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.IdempotencyOperation;
import com.nhatbui.wallet_ledger.enums.TransactionStatus;
import com.nhatbui.wallet_ledger.enums.WalletStatus;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.mapper.TransferMapper;
import com.nhatbui.wallet_ledger.repository.LedgerEntryRepository;
import com.nhatbui.wallet_ledger.repository.TransactionRepository;
import com.nhatbui.wallet_ledger.repository.WalletRepository;
import com.nhatbui.wallet_ledger.service.idempotency.IdempotentExecutor;
import com.nhatbui.wallet_ledger.service.idempotency.RequestHasher;
import com.nhatbui.wallet_ledger.service.transaction.DepositService;
import com.nhatbui.wallet_ledger.service.wallet.SystemWalletService;
import lombok.RequiredArgsConstructor;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import com.nhatbui.wallet_ledger.service.notification.BalanceChangeNotifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private static final TypeReference<ApiResponse<TransferResponse>> RESPONSE_TYPE = new TypeReference<>() {
    };
    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;
    private final SystemWalletService systemWalletService;
    private final WalletRepository walletRepository;
    private final SecurityUtils securityUtils;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionFactory transactionFactory;
    private final IdempotentExecutor idempotentExecutor;
    private final RequestHasher requestHasher;
    private final BalanceChangeNotifier balanceChangeNotifier;

    @Override
    @PreAuthorize("isAuthenticated()")
    public IdempotentResult<ApiResponse<TransferResponse>> deposit(DepositRequest depositRequest) {
        UUID userId = securityUtils.getCurrentUserId();
        String hash = requestHasher.hash(IdempotencyOperation.DEPOSIT, depositRequest.getWalletNumber(), depositRequest.getAmount(), depositRequest.getDescription());

        return idempotentExecutor.execute(
                userId,
                IdempotencyOperation.DEPOSIT,
                depositRequest.getIdempotencyKey(),
                hash,
                RESPONSE_TYPE,
                () -> doDeposit(depositRequest, userId));
    }

    private ApiResponse<TransferResponse> doDeposit(DepositRequest depositRequest, UUID userId) {
        if (depositRequest.getAmount() < 1000L) {
            throw new AppException(ErrorCode.INVALID_DEPOSIT_AMOUNT);
        }

        Wallet systemWallet = systemWalletService.getSystemWallet();

        Wallet userWallet = walletRepository
                .findByWalletNumberAndUserUid(
                        depositRequest.getWalletNumber(),
                        userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        List<Wallet> locked = lockInOrder(systemWallet, userWallet);
        Wallet lockedSystem = locked.get(0).getWalletNumber().equals(systemWallet.getWalletNumber())
                ? locked.get(0) : locked.get(1);
        Wallet lockedUser = locked.get(0).getWalletNumber().equals(userWallet.getWalletNumber())
                ? locked.get(0) : locked.get(1);

        if (lockedUser.getStatus() != WalletStatus.ACTIVE) {
            throw new AppException(ErrorCode.fromWalletStatus(lockedUser.getStatus()));
        }

        Instant now = Instant.now();

        Transaction transaction = transactionFactory.createDeposit(
                userId, lockedSystem, lockedUser, depositRequest.getAmount(),
                depositRequest.getIdempotencyKey(), depositRequest.getDescription());

        transactionRepository.save(transaction);

        Long systemBalanceBefore = lockedSystem.getBalance();
        Long userBalanceBefore = lockedUser.getBalance();
        Long systemBalanceAfter = systemBalanceBefore - depositRequest.getAmount();
        Long userBalanceAfter = userBalanceBefore + depositRequest.getAmount();

        LedgerEntry debitSystem = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedSystem)
                .entryType(EntryType.DEBIT)
                .amount(depositRequest.getAmount())
                .balanceBefore(systemBalanceBefore)
                .balanceAfter(systemBalanceAfter)
                .createdAt(now)
                .build();

        LedgerEntry creditUser = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(lockedUser)
                .entryType(EntryType.CREDIT)
                .amount(depositRequest.getAmount())
                .balanceBefore(userBalanceBefore)
                .balanceAfter(userBalanceAfter)
                .createdAt(now)
                .build();

        ledgerEntryRepository.saveAll(List.of(debitSystem, creditUser));

        lockedSystem.setBalance(systemBalanceAfter);
        lockedSystem.setAvailableBalance(systemBalanceAfter);
        lockedUser.setBalance(userBalanceAfter);
        lockedUser.setAvailableBalance(userBalanceAfter);

        walletRepository.saveAll(List.of(lockedSystem, lockedUser));

        balanceChangeNotifier.notifyBalanceChanged(
                lockedUser, EntryType.CREDIT, TransactionType.DEPOSIT, depositRequest.getAmount());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());

        transactionRepository.save(transaction);

        return ApiResponse.<TransferResponse>builder()
                .success(true)
                .message("Nạp tiền thành công")
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
