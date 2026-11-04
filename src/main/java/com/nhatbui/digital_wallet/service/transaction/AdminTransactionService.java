package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminTransactionService {
    PageResponse<TransactionAdminResponse> getTransactions(Pageable pageable, TransactionSearchRequest searchRequest);

    ApiResponse<TransactionAdminDetailResponse> getTransactionDetail(UUID id);
}