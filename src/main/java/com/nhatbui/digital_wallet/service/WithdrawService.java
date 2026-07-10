package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;

public interface WithdrawService {
    public ApiResponse<TransferResponse> withdraw(WithdrawRequest request);
}
