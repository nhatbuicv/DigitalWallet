package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;

public interface WithdrawService {
    IdempotentResult<ApiResponse<TransferResponse>> withdraw(WithdrawRequest request);
}
