package com.nhatbui.wallet_ledger.service.transaction;

import com.nhatbui.wallet_ledger.dto.request.wallet.TransferRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.IdempotentResult;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;

public interface TransferService {
    IdempotentResult<ApiResponse<TransferResponse>> transfer(TransferRequest request);
}
