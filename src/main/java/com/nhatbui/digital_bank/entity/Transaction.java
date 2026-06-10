package com.nhatbui.digital_bank.entity;

import com.nhatbui.digital_bank.enums.CurrencyCode;
import com.nhatbui.digital_bank.enums.TransactionStatus;
import com.nhatbui.digital_bank.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

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
    private String id;

    @Column(name = "reference_code", unique = true, nullable = false, length = 32)
    private String referenceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column( nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

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
