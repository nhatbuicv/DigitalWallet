package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.TransactionType;
import com.nhatbui.digital_wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final SecurityUtils securityUtils;

    @Override
    @PreAuthorize("isAuthenticated()")
    public PageResponse<TransactionHistoryResponse> getMyTransactionHistory(Pageable pageable , EntryType entryType , Instant fromDate , Instant toDate) {
        var userID = securityUtils.getCurrentUserId();

        return null;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TransactionDetailResponse getTransactionDetail(String transactionId) {
        return null;
    }
}
