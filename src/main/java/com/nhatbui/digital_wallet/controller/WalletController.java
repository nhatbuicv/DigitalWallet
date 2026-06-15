package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.CreateWalletRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletSummaryResponse;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletSummaryResponse>>> getMyAccounts() {
        UUID currentUserId = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getAccountsByUserUid(currentUserId));
    }

    @GetMapping("/{accountNumber}/detail")
    public ResponseEntity<ApiResponse<WalletResponse>> getAccountByAccountNumber(@PathVariable String accountNumber) {
        UUID currentUserUid = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getAccountByAccountNumber(accountNumber, currentUserUid));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createAccount(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.createAccount(request));
    }

    @PatchMapping("/{accountId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@PathVariable UUID accountId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.changeAccountStatus(accountId, WalletStatus.ACTIVE));
    }

    @PatchMapping("/{accountId}/freeze")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(@PathVariable UUID accountId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.changeAccountStatus(accountId, WalletStatus.FROZEN));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletService.getBalance(accountNumber));
    }
}