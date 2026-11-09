package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;

public interface TransferService {
    IdempotentResult<ApiResponse<TransferResponse>> transfer(TransferRequest request);
}
