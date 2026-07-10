package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.CreateWalletRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.BalanceResponse;

import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletSummaryResponse>>> getMyWallets() {
        UUID currentUserId = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getWalletsByUserUid(currentUserId));
    }

    @GetMapping("/{walletNumber}/detail")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByWalletNumber(@PathVariable String walletNumber) {
        UUID currentUserUid = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getWalletByWalletNumber(walletNumber, currentUserUid));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.createWallet(request));
    }

    @PatchMapping("/{walletId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWallet(@PathVariable UUID walletId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.changeWalletStatus(walletId, WalletStatus.ACTIVE));
    }

    @PatchMapping("/{walletId}/freeze")
    public ResponseEntity<ApiResponse<Void>> freezeWallet(@PathVariable UUID walletId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.changeWalletStatus(walletId, WalletStatus.FROZEN));
    }

    @GetMapping("/{walletNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(@PathVariable String walletNumber) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getBalance(walletNumber));
    }
}