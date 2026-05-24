package com.nhatbui.digital_bank.entity;

import com.nhatbui.digital_bank.enums.AccountStatus;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uid", nullable = false)
    User user;

    @Column(unique = true, nullable = false, length = 12)
    String accountNumber;

    @Column(nullable = false, length = 100)
    String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    CurrencyCode currency = CurrencyCode.VND;

    @Column(nullable = false)
    @Builder.Default
    BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    BigDecimal blockedBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    BigDecimal interestRate = BigDecimal.ZERO;

    @Builder.Default
    BigDecimal overdraftLimit = BigDecimal.ZERO;

    @Builder.Default
    BigDecimal minimumBalance = BigDecimal.ZERO;

    BigDecimal dailyLimit;

    BigDecimal monthlyLimit;

    @Column(length = 10)
    String branchCode;

    @Column(length = 100)
    String branchName;

    Instant openedAt;

    Instant closedAt;

    String closedReason;

    @CreationTimestamp
    @Column(updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @Version
    Integer version;
}
