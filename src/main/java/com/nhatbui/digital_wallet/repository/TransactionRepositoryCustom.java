package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.repository.projection.TransactionAdminProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TransactionRepositoryCustom {

    Page<TransactionAdminProjection> findProjectedBySpec(Specification<Transaction> spec, Pageable pageable);
}
