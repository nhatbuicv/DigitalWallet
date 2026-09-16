package com.nhatbui.digital_wallet.entity;

import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID uid;

    @Column(name = "actor_uid", nullable = false)
    UUID actorUid;

    @Column(name = "actor_name", length = 100)
    String actorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    AuditTargetType targetType;

    @Column(name = "target_uid", nullable = false)
    UUID targetUid;

    @Column(name = "target_identifier", length = 255)
    String targetIdentifier;

    @Column(columnDefinition = "TEXT")
    String detail;

    @Column(nullable = false)
    Instant createdAt;
}
