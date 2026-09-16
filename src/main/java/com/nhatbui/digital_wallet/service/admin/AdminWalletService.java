package com.nhatbui.digital_wallet.service.admin;

import com.nhatbui.digital_wallet.dto.request.admin.WalletSearchRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WalletStatusRequest;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminWalletService {

    PageResponse<WalletAdminResponse> getWallets(Pageable pageable, WalletSearchRequest searchRequest);

    ApiResponse<WalletAdminResponse> getWalletDetail(UUID walletUid);

    ApiResponse<WalletAdminResponse> freezeWallet(UUID walletUid, WalletStatusRequest statusRequest);

    ApiResponse<WalletAdminResponse> unfreezeWallet(UUID walletUid, WalletStatusRequest statusRequest);
}
