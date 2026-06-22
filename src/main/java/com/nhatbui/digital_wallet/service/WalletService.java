package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.CreateWalletRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.WalletStatus;

import java.util.List;
import java.util.UUID;


public interface WalletService {
    Wallet createDefaultWallet(User user);

    ApiResponse<List<WalletSummaryResponse>> getWalletsByUserUid(UUID uid);

    ApiResponse<WalletResponse> getWalletByWalletNumber(String walletNumber , UUID userUid);

    ApiResponse<WalletResponse> createWallet(CreateWalletRequest request);

    ApiResponse<Void> changeWalletStatus(UUID walletId, WalletStatus status);

    ApiResponse<BalanceResponse> getBalance(String walletNumber);


}
