package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;
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
    public ApiResponse<TransferResponse> transfer(TransferRequest request) {

        // Idempotency check
        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return ApiResponse.<TransferResponse>builder()
                    .success(true)
                    .message("Chuyển tiền thành công")
                    .data(transferMapper.mapToTransferResponse(existing.get()))
                    .build();
        }

        // Không thể chuyển cho chính mình
        if (request.getFromAccountNumber().equals(request.getToAccountNumber()))
            throw new AppException(ErrorCode.TRANSFER_SAME_WALLET);

        User user = securityUtils.getCurrentUser();

        // Validate quyền sở hữu ví nguồn (không cần lock lúc này)
        Wallet fromWallet = walletRepository.findByAccountNumberAndUserUid(
                        request.getFromAccountNumber(), user.getUid())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet toWallet = walletRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        // Lấy UID để sort trước khi lock → tránh deadlock
        UUID fromUid = fromWallet.getUid();
        UUID toUid   = toWallet.getUid();

        // Re-fetch WITH pessimistic lock theo thứ tự UID tăng dần
        Wallet lockedFirst  = fromUid.compareTo(toUid) < 0
                ? walletRepository.findByUidWithLock(fromUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND))
                : walletRepository.findByUidWithLock(toUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet lockedSecond = fromUid.compareTo(toUid) < 0
                ? walletRepository.findByUidWithLock(toUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND))
                : walletRepository.findByUidWithLock(fromUid).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        // Sau khi giữ lock, resolve lại đúng vai trò từng ví
        fromWallet = lockedFirst.getUid().equals(fromUid) ? lockedFirst : lockedSecond;
        toWallet   = lockedFirst.getUid().equals(toUid)   ? lockedFirst : lockedSecond;

        // Validate status và balance SAU KHI đã giữ lock
        if (fromWallet.getStatus() != WalletStatus.ACTIVE)
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);

        if (toWallet.getStatus() != WalletStatus.ACTIVE)
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);

        // Tạo transaction ở trạng thái PENDING
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

        // Ghi ledger + cập nhật số dư - tài khoản gửi (DEBIT)
        var fromBalanceBefore = fromWallet.getBalance();
        var fromBalanceAfter  = fromBalanceBefore.subtract(request.getAmount());
        fromWallet.setBalance(fromBalanceAfter);
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

        // Ghi ledger + cập nhật số dư - tài khoản nhận (CREDIT)
        var toBalanceBefore = toWallet.getBalance();
        var toBalanceAfter  = toBalanceBefore.add(request.getAmount());
        toWallet.setBalance(toBalanceAfter);
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

        // Cập nhật trạng thái SUCCESS
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
        return fromWallet.getAccountName() + " chuyển tiền";
    }
}