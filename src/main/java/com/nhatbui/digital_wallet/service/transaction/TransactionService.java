package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.TransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface TransactionService {
    PageResponse<TransactionHistoryResponse> getMyTransactionHistory(Pageable pageable , EntryType entryType , Instant fromDate , Instant toDate);

    TransactionDetailResponse getTransactionDetail(String transactionId);
}
