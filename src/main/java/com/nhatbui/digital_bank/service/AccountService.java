package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.CurrencyCode;
import com.nhatbui.digital_bank.generator.AccountNumberGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;


public interface AccountService {
    Account createDefaultAccount(User user);
}
