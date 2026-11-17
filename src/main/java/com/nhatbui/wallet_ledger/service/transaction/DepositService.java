package com.nhatbui.wallet_ledger.service.transaction;

import com.nhatbui.wallet_ledger.dto.request.wallet.DepositRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.IdempotentResult;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;

public interface DepositService {
    IdempotentResult<ApiResponse<TransferResponse>> deposit(DepositRequest depositRequest);
}
