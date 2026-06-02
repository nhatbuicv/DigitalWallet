package com.nhatbui.digital_bank.entity;

import com.nhatbui.digital_bank.enums.UserRole;
import com.nhatbui.digital_bank.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID uid;

    @Column(unique = true, nullable = false, length = 50)
    String customerId;

    @Column(nullable = false, length = 100)
    String fullName;

    @Column(unique = true, length = 15)
    String phone;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code")
    Province province;

    LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    UserRole role = UserRole.USER;

    @Builder.Default
    Boolean phoneVerified = false;

    @Builder.Default
    Integer failedLoginAttempts = 0; // đăng nhập sai quá 5 lần khóa tài khoản

    @Builder.Default
    Boolean accountNonLocked = true;

    Instant lockTime;

    Instant lastLogin;

    @CreationTimestamp
    @Column(updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @OneToMany(mappedBy = "user")
    List<Account> accounts;

    // User.java
    @Builder.Default
    Integer tokenVersion = 0;
}
