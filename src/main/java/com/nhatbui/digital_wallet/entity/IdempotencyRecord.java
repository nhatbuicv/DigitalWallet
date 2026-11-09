package com.nhatbui.digital_wallet.entity;

import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idem_user_op_key",
                columnNames = {"user_id", "operation", "idem_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdempotencyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    IdempotencyOperation operation;

    @Column(name = "idem_key", nullable = false, length = 64)
    String idemKey;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "request_hash", nullable = false, length = 64, columnDefinition = "char(64)")
    String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    IdempotencyStatus status;

    @Column(name = "http_status")
    Integer httpStatus;

    @Column(name = "response_body", columnDefinition = "text")
    String responseBody;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;
}
