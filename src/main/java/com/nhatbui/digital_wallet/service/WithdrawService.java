package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.TransferResponse;

public interface WithdrawService {
    public ApiResponse<TransferResponse> withdraw(WithdrawRequest request);
}
