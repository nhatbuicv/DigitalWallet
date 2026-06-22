package com.nhatbui.digital_wallet.entity;

import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "reference_code", unique = true, nullable = false, length = 32)
    private String referenceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Column( nullable = false, precision = 19, scale = 4)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 3)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 20)
    private TransactionStatus status;

    @Column( length = 255)
    private String description;

    @Column( unique = true, nullable = false, length = 64)
    private String idempotencyKey;

    @Column( nullable = false, updatable = false)
    private Instant createdAt;

    private Instant completedAt;

}
