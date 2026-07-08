package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.TransferRequest;
import com.nhatbui.digital_wallet.dto.request.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;
import com.nhatbui.digital_wallet.service.DepositService;
import com.nhatbui.digital_wallet.service.TransferService;
import com.nhatbui.digital_wallet.service.WithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransferService transferService;
    private final DepositService depositService;
    private final WithdrawService withdrawService;

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
}