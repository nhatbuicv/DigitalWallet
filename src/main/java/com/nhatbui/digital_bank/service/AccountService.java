package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.CreateAccountRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.BalanceResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountSummaryResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.AccountStatus;

import java.util.List;
import java.util.UUID;


public interface AccountService {
    Account createDefaultAccount(User user);

    ApiResponse<List<AccountSummaryResponse>> getAccountsByUserUid(UUID uid);

    ApiResponse<AccountResponse> getAccountByAccountNumber(String accountNumber , UUID userUid);

    ApiResponse<AccountResponse> createAccount(CreateAccountRequest request);

    ApiResponse<Void> changeAccountStatus(UUID accountId, AccountStatus status);

    ApiResponse<BalanceResponse> getBalance(String accountNumber);


}
