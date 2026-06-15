package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.CreateWalletRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.WalletStatus;

import java.util.List;
import java.util.UUID;


public interface WalletService {
    Wallet createDefaultAccount(User user);

    ApiResponse<List<WalletSummaryResponse>> getAccountsByUserUid(UUID uid);

    ApiResponse<WalletResponse> getAccountByAccountNumber(String accountNumber , UUID userUid);

    ApiResponse<WalletResponse> createAccount(CreateWalletRequest request);

    ApiResponse<Void> changeAccountStatus(UUID accountId, WalletStatus status);

    ApiResponse<BalanceResponse> getBalance(String accountNumber);


}
