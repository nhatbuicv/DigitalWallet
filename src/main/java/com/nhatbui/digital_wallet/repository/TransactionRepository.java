package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> , JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @EntityGraph(attributePaths = {"fromWallet", "fromWallet.user", "toWallet", "toWallet.user"})
    @Query("select t from Transaction t where t.id = :id")
    Optional<Transaction> findDetailById(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"fromWallet", "fromWallet.user", "toWallet", "toWallet.user"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    long countByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}
