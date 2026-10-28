package com.nhatbui.digital_wallet.entity;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uid", nullable = true)
    User user;

    @Column(unique = true, nullable = false, length = 12)
    String walletNumber;

    @Column(nullable = false, length = 100)
    String walletName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    WalletType walletType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    CurrencyCode currency = CurrencyCode.VND;

    @Column(nullable = false)
    @Builder.Default
    Long balance = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long availableBalance = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long blockedBalance = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    WalletStatus status = WalletStatus.ACTIVE;

    @Builder.Default
    Long interestRate = 0L;

    @Builder.Default
    Long overdraftLimit = 0L;

    @Builder.Default
    Long minimumBalance = 0L;

    // Transaction limits. Null for the SYSTEM wallet (intermediary account),
    // which is not subject to customer transaction limits.
    Long dailyLimit;

    Long monthlyLimit;

    Long singleTransactionLimit;

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
