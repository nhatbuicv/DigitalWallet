package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.request.wallet.TransactionHistoryRequest;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    PageResponse<TransactionHistoryResponse> getMyTransactionHistory(
            Pageable pageable,
            TransactionHistoryRequest request);

    TransactionDetailResponse getTransactionDetail(UUID transactionId);
}
