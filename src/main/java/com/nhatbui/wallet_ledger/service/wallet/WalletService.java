package com.nhatbui.wallet_ledger.service.wallet;

import com.nhatbui.wallet_ledger.dto.request.wallet.CreateWalletRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.BalanceResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.WalletResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.enums.WalletStatus;

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
