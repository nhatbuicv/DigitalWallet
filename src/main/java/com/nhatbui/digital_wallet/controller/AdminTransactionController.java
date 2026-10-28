package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminResponse;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import com.nhatbui.digital_wallet.service.transaction.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionAdminResponse>>> getTransactions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {

        TransactionSearchRequest searchRequest = TransactionSearchRequest.builder()
                .reference(reference)
                .status(status)
                .type(type)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        PageResponse<TransactionAdminResponse> data =
                adminTransactionService.getTransactions(pageable, searchRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<TransactionAdminResponse>>builder()
                        .message("Lấy danh sách giao dịch thành công")
                        .success(true)
                        .data(data)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionAdminDetailResponse>> getTransactionDetail(@PathVariable UUID id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminTransactionService.getTransactionDetail(id));
    }
}
