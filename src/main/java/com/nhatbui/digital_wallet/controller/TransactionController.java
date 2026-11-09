package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransactionHistoryRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
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

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENT_REPLAY_HEADER = "Idempotent-Replay";

    private final TransferService transferService;
    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String headerKey,
            @Valid @RequestBody TransferRequest request) {
        request.setIdempotencyKey(resolveIdempotencyKey(headerKey, request.getIdempotencyKey()));
        return toResponse(transferService.transfer(request));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransferResponse>> deposit(
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String headerKey,
            @Valid @RequestBody DepositRequest request) {
        request.setIdempotencyKey(resolveIdempotencyKey(headerKey, request.getIdempotencyKey()));
        return toResponse(depositService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransferResponse>> withdraw(
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String headerKey,
            @Valid @RequestBody WithdrawRequest request) {
        request.setIdempotencyKey(resolveIdempotencyKey(headerKey, request.getIdempotencyKey()));
        return toResponse(withdrawService.withdraw(request));
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

    private String resolveIdempotencyKey(String headerKey, String bodyKey) {
        String normalizedHeader = normalize(headerKey);
        String normalizedBody = normalize(bodyKey);

        if (normalizedHeader != null && normalizedBody != null && !normalizedHeader.equals(normalizedBody)) {
            throw new AppException(ErrorCode.INVALID_REQUEST_BODY);
        }

        String resolved = normalizedHeader != null ? normalizedHeader : normalizedBody;
        if (resolved == null) {
            throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }
        if (resolved.length() > 64) {
            throw new AppException(ErrorCode.INVALID_REQUEST_BODY);
        }
        return resolved;
    }

    private String normalize(String key) {
        if (key == null) {
            return null;
        }
        String trimmed = key.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> ResponseEntity<ApiResponse<T>> toResponse(IdempotentResult<ApiResponse<T>> result) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.OK);
        if (result.replayed()) {
            builder.header(IDEMPOTENT_REPLAY_HEADER, "true");
        }
        return builder.body(result.body());
    }
}
