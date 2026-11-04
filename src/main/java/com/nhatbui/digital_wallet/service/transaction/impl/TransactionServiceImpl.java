package com.nhatbui.digital_wallet.service.transaction.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.TransactionHistoryRequest;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.TransactionMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final SecurityUtils securityUtils;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PageResponse<TransactionHistoryResponse> getMyTransactionHistory(Pageable pageable, TransactionHistoryRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        TransactionHistoryRequest filter = request != null ? request : new TransactionHistoryRequest();
        validateDateRange(filter);

        Page<LedgerEntry> page = ledgerEntryRepository.findHistoryByUserId(userId, filter.getEntryType(), filter.getFromDate(), filter.getToDate(), normalizePageable(pageable));

        return PageResponse.from(page, transactionMapper::toHistoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TransactionDetailResponse getTransactionDetail(UUID transactionId) {
        UUID userId = securityUtils.getCurrentUserId();
        List<LedgerEntry> entries = ledgerEntryRepository.findDetailByTransactionIdAndUserId(transactionId, userId);
        if (entries.isEmpty()) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        return transactionMapper.toDetailResponse(getUserLedgerEntry(entries));
    }

    private void validateDateRange(TransactionHistoryRequest request) {
        if (request.getFromDate() != null && request.getToDate() != null && request.getFromDate().isAfter(request.getToDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE);
        }

        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.min(Math.max(1, pageable.getPageSize()), MAX_PAGE_SIZE);

        return PageRequest.of(page, size);
    }

    /**
     * 1 transaction tao ra 2 ledger entry thuộc 1 yser
     * nên cần chọn cái debit (trừ tiền)
    */
    private LedgerEntry getUserLedgerEntry(List<LedgerEntry> entries) {
        return entries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.DEBIT)
                .findFirst()
                .orElse(entries.get(0));
    }
}
