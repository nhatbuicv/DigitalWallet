package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.TransferRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.TransferResponse;

public interface TransferService {
    ApiResponse<TransferResponse> transfer(TransferRequest request);
}
