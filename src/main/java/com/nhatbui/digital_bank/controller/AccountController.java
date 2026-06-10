package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.config.SecurityUtils;
import com.nhatbui.digital_bank.dto.request.CreateAccountRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.BalanceResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountSummaryResponse;
import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.service.AccountService;
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
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountSummaryResponse>>> getMyAccounts() {
        UUID currentUserId = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.getAccountsByUserUid(currentUserId));
    }

    @GetMapping("/{accountNumber}/detail")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNumber(@PathVariable String accountNumber) {
        UUID currentUserUid = securityUtils.getCurrentUser().getUid();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.getAccountByAccountNumber(accountNumber, currentUserUid));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @PatchMapping("/{accountId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@PathVariable UUID accountId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.changeAccountStatus(accountId, AccountStatus.ACTIVE));
    }

    @PatchMapping("/{accountId}/freeze")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(@PathVariable UUID accountId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.changeAccountStatus(accountId, AccountStatus.FROZEN));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.getBalance(accountNumber));
    }
}