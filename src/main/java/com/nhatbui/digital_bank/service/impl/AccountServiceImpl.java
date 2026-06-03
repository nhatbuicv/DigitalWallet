package com.nhatbui.digital_bank.service.impl;

import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.CurrencyCode;
import com.nhatbui.digital_bank.generator.AccountNumberGenerator;
import com.nhatbui.digital_bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public Account createDefaultAccount (User user){
        String accountNumber = accountNumberGenerator.generate(null, null);

        return Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountName("Tài khoản thanh toán")
                .accountType(AccountType.CHECKING)
                .currency(CurrencyCode.VND)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .status(AccountStatus.PENDING)
                .branchCode("099")
                .branchName("Online Branch")
                .dailyLimit(new BigDecimal("50000000"))
                .monthlyLimit(new BigDecimal("500000000"))
                .openedAt(Instant.now())
                .build();
    }
}
