package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.dto.request.TransferRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.TransferResponse;
import com.nhatbui.digital_bank.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        ApiResponse<TransferResponse> response = transferService.transfer(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }
}