package com.nhatbui.digital_bank.service.impl;

import com.nhatbui.digital_bank.config.SecurityUtils;
import com.nhatbui.digital_bank.dto.request.CreateAccountRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.BalanceResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountSummaryResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.CurrencyCode;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.generator.AccountNumberGenerator;
import com.nhatbui.digital_bank.mapper.AccountMapper;
import com.nhatbui.digital_bank.repository.AccountRepository;
import com.nhatbui.digital_bank.repository.UserRepository;
import com.nhatbui.digital_bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.nhatbui.digital_bank.enums.AccountStatus.*;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountNumberGenerator accountNumberGenerator;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final SecurityUtils securityUtils;

    @Override
    public Account createDefaultAccount(User user) {
        String accountNumber = accountNumberGenerator.generate(null, null);

        return Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountName(user.getFullName())
                .accountType(AccountType.CHECKING)
                .currency(CurrencyCode.VND)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .status(PENDING)
                .branchCode("099")
                .branchName("Online Branch")
                .dailyLimit(new BigDecimal("50000000"))
                .monthlyLimit(new BigDecimal("500000000"))
                .openedAt(Instant.now())
                .build();
    }


    @Override
    public ApiResponse<List<AccountSummaryResponse>> getAccountsByUserUid(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        List<Account> accounts = accountRepository.findAllByUserUid(userId);
        List<AccountSummaryResponse> data = accountMapper.mapToAccountSummaryResponse(accounts);

        return ApiResponse.<List<AccountSummaryResponse>>builder()
                .success(true)
                .message("Thành công")
                .data(data)
                .build();
    }

    @Override
    public ApiResponse<AccountResponse> getAccountByAccountNumber(String accountNumber, UUID userUid) {
        Account account = accountRepository.findByAccountNumberAndUserUid(accountNumber, userUid)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        AccountResponse data = accountMapper.mapToAccountResponse(account);

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Thành công")
                .data(data)
                .build();
    }

    @Override
    public ApiResponse<AccountResponse> createAccount(CreateAccountRequest request) {
        User user = securityUtils.getCurrentUser();

        String accountNumber = accountNumberGenerator.generate(null, request.getAccountType());

        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountName(user.getFullName())
                .accountType(request.getAccountType())
                .currency(CurrencyCode.VND)
                .status(ACTIVE)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .branchCode("099")
                .branchName("Online Branch")
                .dailyLimit(new BigDecimal("50000000"))
                .monthlyLimit(new BigDecimal("500000000"))
                .openedAt(Instant.now())
                .openedAt(Instant.now())
                .build();

        accountRepository.save(account);

        AccountResponse data = accountMapper.mapToAccountResponse(account);

        return ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Tạo tài khoản thành công")
                .data(data)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> changeAccountStatus(UUID accountId, AccountStatus newStatus) {
        User user = securityUtils.getCurrentUser();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getUid().equals(user.getUid())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        AccountStatus currentStatus = account.getStatus();

        if (currentStatus == AccountStatus.CLOSED) {
            throw new AppException(ErrorCode.ACCOUNT_CLOSED);
        }

        if (currentStatus == AccountStatus.INACTIVE || currentStatus == AccountStatus.PENDING || currentStatus == AccountStatus.SUSPENDED) {
            throw new AppException(ErrorCode.ACCOUNT_STATUS_NOT_CHANGEABLE);
        }

        if (newStatus == AccountStatus.ACTIVE) {
            if (currentStatus != AccountStatus.FROZEN) {
                throw new AppException(ErrorCode.ACCOUNT_NOT_FROZEN);
            }
            account.setStatus(AccountStatus.ACTIVE);

        } else if (newStatus == AccountStatus.FROZEN) {
            if (currentStatus != AccountStatus.ACTIVE) {
                throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
            }

            long activeCount = accountRepository.countByUserUidAndStatus(user.getUid(), AccountStatus.ACTIVE);
            if (activeCount <= 1) {
                throw new AppException(ErrorCode.CANNOT_FREEZE_LAST_ACTIVE_ACCOUNT);
            }
            account.setStatus(AccountStatus.FROZEN);

        } else {
            throw new AppException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        accountRepository.save(account);

        return ApiResponse.<Void>builder()
                .success(true)
                .message(newStatus == AccountStatus.ACTIVE ? "Kích hoạt tài khoản thành công" : "Đóng băng tài khoản thành công")
                .build();
    }

    @Override
    public ApiResponse<BalanceResponse> getBalance(String accountNumber) {
        User user = securityUtils.getCurrentUser();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getUid().equals(user.getUid())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        switch (account.getStatus()) {
            case CLOSED -> throw new AppException(ErrorCode.ACCOUNT_CLOSED);
            case SUSPENDED -> throw new AppException(ErrorCode.ACCOUNT_SUSPENDED);
            case INACTIVE, PENDING -> throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
            case ACTIVE, FROZEN -> {}
            default -> throw new AppException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        BalanceResponse data = accountMapper.mapToBalanceResponse(account);

        return ApiResponse.<BalanceResponse>builder()
                .success(true)
                .message("Lấy số dư thành công")
                .data(data)
                .build();
    }
}
