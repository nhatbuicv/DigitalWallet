package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.IdempotencyRecord;
import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.enums.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByUserIdAndOperationAndIdemKey(UUID userId, IdempotencyOperation operation, String idemKey);

    /**
     * Claim-first: insert IN_PROGRESS, nếu key đã tồn tại thì không làm gì.
     * Trả về 1 nếu request này giành được claim, 0 nếu key đã tồn tại.
     */
    @Modifying
    @Query(value = """
            INSERT INTO idempotency_records
                (id, user_id, operation, idem_key, request_hash,
                 status, created_at, updated_at, expires_at)
            VALUES
                (:id, :userId, :operation, :idemKey, :requestHash,'IN_PROGRESS', :now, :now, :expiresAt)
            ON CONFLICT (user_id, operation, idem_key) DO NOTHING""", nativeQuery = true)
    int tryInsert(@Param("id") UUID id, @Param("userId") UUID userId, @Param("operation") String operation, @Param("idemKey") String idemKey,
                  @Param("requestHash") String requestHash, @Param("now") Instant now, @Param("expiresAt") Instant expiresAt);

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE IdempotencyRecord r
               SET r.status = :status,
                   r.httpStatus = :httpStatus,
                   r.responseBody = :responseBody,
                   r.updatedAt = :now
             WHERE r.id = :id
            """)
    int markFinished(@Param("id") UUID id, @Param("status") IdempotencyStatus status, @Param("httpStatus") Integer httpStatus, @Param("responseBody") String responseBody, @Param("now") Instant now);


    // xóa bản ghi đã có kết quả cuối cùng (COMPLETED/FAILED) và đã hết TTL.
    @Modifying
    @Query("""
            DELETE FROM IdempotencyRecord r
             WHERE r.expiresAt < :now
               AND r.status IN :finalStatuses
            """)
    int deleteExpired(@Param("now") Instant now, @Param("finalStatuses") Collection<IdempotencyStatus> finalStatuses);

    // Các bản ghi IN_PROGRESS quá hạn chỉ được chuyển sang SUSPECTED_TIMEOUT, giữ nguyên unique constraint để request retry không thể thực thi lại.
    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE IdempotencyRecord r
               SET r.status = :timeoutStatus,
                   r.updatedAt = :now
             WHERE r.status = :inProgress
               AND r.updatedAt < :cutoff
            """)
    int markStaleAsTimeout(@Param("cutoff") Instant cutoff, @Param("inProgress") IdempotencyStatus inProgress, @Param("timeoutStatus") IdempotencyStatus timeoutStatus, @Param("now") Instant now);
}
