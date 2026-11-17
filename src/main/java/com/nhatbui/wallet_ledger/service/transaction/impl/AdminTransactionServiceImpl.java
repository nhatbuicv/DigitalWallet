package com.nhatbui.wallet_ledger.service.transaction.impl;

import com.nhatbui.wallet_ledger.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminResponse;
import com.nhatbui.wallet_ledger.entity.LedgerEntry;
import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.mapper.TransactionMapper;
import com.nhatbui.wallet_ledger.repository.LedgerEntryRepository;
import com.nhatbui.wallet_ledger.repository.TransactionRepository;
import com.nhatbui.wallet_ledger.repository.projection.TransactionAdminProjection;
import com.nhatbui.wallet_ledger.repository.specification.TransactionSpecification;
import com.nhatbui.wallet_ledger.service.transaction.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public PageResponse<TransactionAdminResponse> getTransactions(Pageable pageable, TransactionSearchRequest searchRequest) {
        Specification<Transaction> spec = TransactionSpecification.buildSearchSpec(searchRequest);
        Page<TransactionAdminProjection> transactionPage = transactionRepository.findProjectedBySpec(spec, pageable);

        return PageResponse.from(transactionPage, transactionMapper::mapToAdminResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ApiResponse<TransactionAdminDetailResponse> getTransactionDetail(UUID id) {
        Transaction transaction = transactionRepository.findDetailById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        TransactionAdminDetailResponse response = transactionMapper.mapToAdminDetailResponse(transaction);

        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAllByTransaction_IdOrderByCreatedAtAsc(transaction.getId());
        response.setLedgerEntries(ledgerEntries.stream()
                .map(transactionMapper::mapToLedgerEntryAdminResponse)
                .toList());

        return ApiResponse.<TransactionAdminDetailResponse>builder()
                .message("Lấy chi tiết giao dịch thành công")
                .success(true)
                .data(response)
                .build();
    }
}
