package com.nhatbui.wallet_ledger.service.transaction;

import com.nhatbui.wallet_ledger.dto.request.wallet.WithdrawRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.IdempotentResult;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;

public interface WithdrawService {
    IdempotentResult<ApiResponse<TransferResponse>> withdraw(WithdrawRequest request);
}
