package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;

public interface TransferService {
    ApiResponse<TransferResponse> transfer(TransferRequest request);
}
