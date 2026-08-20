package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;

public interface DepositService {
    public ApiResponse<TransferResponse> deposit(DepositRequest depositRequest);
}
