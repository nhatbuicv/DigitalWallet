package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.enums.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>, JpaSpecificationExecutor<LedgerEntry> {

    @Query("""
        select le from LedgerEntry le
        join fetch le.wallet
        where le.transaction.id = :transactionId
        order by le.createdAt asc
        """)
    List<LedgerEntry> findAllByTransaction_IdOrderByCreatedAtAsc(@Param("transactionId") UUID transactionId);

    Page<LedgerEntry> findByWalletUid(UUID walletId, Pageable pageable);

    @Query(value = """
            select le
            from LedgerEntry le
            join fetch le.transaction t
            where le.wallet.user.uid = :userId
              and (:entryType is null or le.entryType = :entryType)
              and (:fromDate is null or le.createdAt >= :fromDate)
              and (:toDate is null or le.createdAt <= :toDate)
            order by le.createdAt desc
            """,
            countQuery = """
            select count(le)
            from LedgerEntry le
            where le.wallet.user.uid = :userId
              and (:entryType is null or le.entryType = :entryType)
              and (:fromDate is null or le.createdAt >= :fromDate)
              and (:toDate is null or le.createdAt <= :toDate)
            """)
    Page<LedgerEntry> findHistoryByUserId(@Param("userId") UUID userId, @Param("entryType") EntryType entryType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, Pageable pageable);

    @Query("""
            select le
            from LedgerEntry le
            join fetch le.transaction t
            left join fetch t.fromWallet fw
            left join fetch fw.user
            left join fetch t.toWallet tw
            left join fetch tw.user
            where t.id = :transactionId
              and le.wallet.user.uid = :userId
            """)
    List<LedgerEntry> findDetailByTransactionIdAndUserId(@Param("transactionId") UUID transactionId, @Param("userId") UUID userId);
}
