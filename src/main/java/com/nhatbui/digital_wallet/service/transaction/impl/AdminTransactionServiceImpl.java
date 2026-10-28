package com.nhatbui.digital_wallet.service.transaction.impl;

import com.nhatbui.digital_wallet.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.TransactionMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.specification.TransactionSpecification;
import com.nhatbui.digital_wallet.service.transaction.AdminTransactionService;
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
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<TransactionAdminResponse> getTransactions(Pageable pageable, TransactionSearchRequest searchRequest) {
        Specification<Transaction> spec = TransactionSpecification.buildSearchSpec(searchRequest);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        return PageResponse.from(transactionPage, transactionMapper::mapToAdminResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
