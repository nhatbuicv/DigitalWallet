package com.nhatbui.digital_bank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uid", nullable = false)
    User user;

    @Column(nullable = false, length = 100)
    String deviceId;

    @Column(length = 100)
    String deviceName;

    @Column(nullable = false)
    Instant expiresAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    Instant createdAt;
}