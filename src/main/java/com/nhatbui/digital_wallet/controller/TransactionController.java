package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransactionHistoryRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.service.transaction.DepositService;
import com.nhatbui.digital_wallet.service.transaction.TransactionService;
import com.nhatbui.digital_wallet.service.transaction.TransferService;
import com.nhatbui.digital_wallet.service.transaction.WithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TransactionController {

    private final TransferService transferService;
    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        ApiResponse<TransferResponse> response = transferService.transfer(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransferResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(depositService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransferResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(withdrawService.withdraw(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<TransactionHistoryResponse>>> getMyTransactionHistory(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) EntryType entryType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {

        TransactionHistoryRequest request = TransactionHistoryRequest.builder()
                .entryType(entryType)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        PageResponse<TransactionHistoryResponse> data =
                transactionService.getMyTransactionHistory(pageable, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<TransactionHistoryResponse>>builder()
                        .message("Lấy lịch sử giao dịch thành công")
                        .success(true)
                        .data(data)
                        .build());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable UUID transactionId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<TransactionDetailResponse>builder()
                        .message("Lấy chi tiết giao dịch thành công")
                        .success(true)
                        .data(transactionService.getTransactionDetail(transactionId))
                        .build());
    }
}
