package com.nhatbui.digital_wallet.service.admin.impl;

import com.nhatbui.digital_wallet.dto.request.admin.WalletSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.WalletMapper;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.repository.specification.WalletSpecification;
import com.nhatbui.digital_wallet.service.admin.AdminWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminWalletServiceImpl implements AdminWalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<WalletAdminResponse> getWallets(Pageable pageable, WalletSearchRequest searchRequest) {
        Specification<Wallet> spec = WalletSpecification.buildSearchSpec(searchRequest);
        Page<Wallet> walletPage = walletRepository.findAll(spec, pageable);

        return PageResponse.from(walletPage, walletMapper::mapToWalletAdminResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WalletAdminResponse> getWalletDetail(UUID walletUid) {
        Wallet wallet = walletRepository.findByUid(walletUid)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        return ApiResponse.<WalletAdminResponse>builder()
                .message("Lấy thông tin ví thành công")
                .success(true)
                .data(walletMapper.mapToWalletAdminResponse(wallet))
                .build();
    }
}