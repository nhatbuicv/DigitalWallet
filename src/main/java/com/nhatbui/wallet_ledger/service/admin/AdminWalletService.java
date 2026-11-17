package com.nhatbui.wallet_ledger.service.admin;

import com.nhatbui.wallet_ledger.dto.request.admin.WalletSearchRequest;
import com.nhatbui.wallet_ledger.dto.request.wallet.WalletStatusRequest;
import com.nhatbui.wallet_ledger.dto.response.admin.WalletAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminWalletService {

    PageResponse<WalletAdminResponse> getWallets(Pageable pageable, WalletSearchRequest searchRequest);

    ApiResponse<WalletAdminResponse> getWalletDetail(UUID walletUid);

    ApiResponse<WalletAdminResponse> freezeWallet(UUID walletUid, WalletStatusRequest statusRequest);

    ApiResponse<WalletAdminResponse> unfreezeWallet(UUID walletUid, WalletStatusRequest statusRequest);
}
