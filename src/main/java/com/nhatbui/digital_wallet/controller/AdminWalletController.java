package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.admin.WalletSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.service.admin.AdminWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WalletAdminResponse>>> getWallets(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String walletNumber,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) WalletStatus status,
            @RequestParam(required = false) WalletType walletType) {

        WalletSearchRequest searchRequest = WalletSearchRequest.builder()
                .walletNumber(walletNumber)
                .userPhone(userPhone)
                .userEmail(userEmail)
                .userName(userName)
                .status(status)
                .walletType(walletType)
                .build();

        PageResponse<WalletAdminResponse> data = adminWalletService.getWallets(pageable, searchRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<WalletAdminResponse>>builder()
                        .message("Lấy danh sách ví thành công")
                        .success(true)
                        .data(data)
                        .build());
    }

    @GetMapping("/{walletUid}")
    public ResponseEntity<ApiResponse<WalletAdminResponse>> getWalletDetail(@PathVariable UUID walletUid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminWalletService.getWalletDetail(walletUid));
    }
}