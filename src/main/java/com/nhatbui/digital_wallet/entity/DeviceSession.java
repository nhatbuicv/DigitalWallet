package com.nhatbui.digital_wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_sessions")
public class DeviceSession {
    @Id
    private String deviceId;

    @ManyToOne
    @JoinColumn(name = "user_uid")
    private User user;

    private String deviceName;
    private Instant lastLoginAt;
    private boolean quickLoginEnabled; // user có bật quick login không
}