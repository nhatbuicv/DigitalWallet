package com.nhatbui.digital_bank.service.impl;

import com.nhatbui.digital_bank.config.SecurityUtils;
import com.nhatbui.digital_bank.dto.request.TransferRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.TransferResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.LedgerEntry;
import com.nhatbui.digital_bank.entity.Transaction;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.*;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.mapper.TransferMapper;
import com.nhatbui.digital_bank.repository.AccountRepository;
import com.nhatbui.digital_bank.repository.LedgerEntryRepository;
import com.nhatbui.digital_bank.repository.TransactionRepository;
import com.nhatbui.digital_bank.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final SecurityUtils securityUtils;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    public ApiResponse<TransferResponse> transfer(TransferRequest request) {

        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if(existing.isPresent()){
            return ApiResponse.<TransferResponse>builder()
                    .success(true)
                    .message("Chuyển tiền thành công")
                    .data(transferMapper.mapToTransferResponse(existing.get()))
                    .build();
        }

        User user = securityUtils.getCurrentUser();

        Account fromAccount = accountRepository.findByAccountNumberAndUserUid(request.getFromAccountNumber(), user.getUid())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE)
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0)
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (toAccount.getStatus() != AccountStatus.ACTIVE)
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);

        if (fromAccount.getAccountNumber().equals(request.getToAccountNumber()))
            throw new AppException(ErrorCode.TRANSFER_SAME_ACCOUNT);

        // sắp xếp lại thứ tự để tránh deadlock
        Account accountFirst =  fromAccount.getUid().compareTo(toAccount.getUid()) < 0 ? fromAccount : toAccount;
        Account accountSecond = fromAccount.getUid().compareTo(toAccount.getUid()) < 0 ? toAccount : fromAccount;
        accountRepository.findByUidWithLock(accountFirst.getUid());
        accountRepository.findByUidWithLock(accountSecond.getUid());

        Transaction transaction = Transaction.builder()
                .referenceCode(generateReferenceCode())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .currency(CurrencyCode.VND)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(generateDescription(fromAccount))
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        transactionRepository.save(transaction);

        // Tai khoan gui
        var fromBalanceBefore = fromAccount.getBalance();
        var fromBalanceAfter = fromAccount.getBalance().subtract(request.getAmount());
        fromAccount.setBalance(fromBalanceAfter);
        accountRepository.save(fromAccount);
        ledgerEntryRepository.save(LedgerEntry.builder()
                        .transaction(transaction)
                        .account(fromAccount)
                        .entryType(EntryType.DEBIT)
                        .amount(request.getAmount())
                        .balanceBefore(fromBalanceBefore)
                        .balanceAfter(fromBalanceAfter)
                        .createdAt(Instant.now())
                .build());


        //Tai khoan nhan
        var toBalanceBefore = toAccount.getBalance();
        var toBalanceAfter  = toBalanceBefore.add(request.getAmount());
        toAccount.setBalance(toBalanceAfter);
        accountRepository.save(toAccount);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transaction(transaction)
                .account(toAccount)
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
        return "TXN" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateDescription(Account account) {
        return account.getAccountName() + "chuyển tiền";
    }
}
