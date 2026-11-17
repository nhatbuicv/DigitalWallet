package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.repository.projection.TransactionAdminProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TransactionRepositoryCustom {

    Page<TransactionAdminProjection> findProjectedBySpec(Specification<Transaction> spec, Pageable pageable);
}
