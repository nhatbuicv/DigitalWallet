package com.nhatbui.wallet_ledger.service.transaction;

import com.nhatbui.wallet_ledger.dto.request.wallet.TransactionHistoryRequest;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransactionHistoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    PageResponse<TransactionHistoryResponse> getMyTransactionHistory(
            Pageable pageable,
            TransactionHistoryRequest request);

    TransactionDetailResponse getTransactionDetail(UUID transactionId);
}
