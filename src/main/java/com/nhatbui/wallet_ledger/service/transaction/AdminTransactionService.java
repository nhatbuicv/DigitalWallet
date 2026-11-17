package com.nhatbui.wallet_ledger.service.transaction;

import com.nhatbui.wallet_ledger.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminTransactionService {
    PageResponse<TransactionAdminResponse> getTransactions(Pageable pageable, TransactionSearchRequest searchRequest);

    ApiResponse<TransactionAdminDetailResponse> getTransactionDetail(UUID id);
}