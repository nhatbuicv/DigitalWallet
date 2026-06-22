package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;

public interface DepositService {
    public ApiResponse<TransferResponse> deposit(DepositRequest depositRequest);
}
