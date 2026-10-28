package com.nhatbui.digital_wallet.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


/**
 * Default transaction limits applied to newly created customer wallets.
 *
 * <p>The legal monthly cap for the normal transaction group (transfer + payment)
 * is 100,000,000 VND per customer, aggregated across all of the customer's wallets.
 * The SYSTEM wallet is an intermediary account and carries no limits.</p>
 */
@Component
@ConfigurationProperties(prefix = "wallet.limits.defaults")
@Validated
@Getter
@Setter
public class WalletLimitProperties {

    /** Per-transaction limit applied to newly created customer wallets (VND). */
    @Min(value = 1_000L, message = "singleTransactionLimit must be at least 1,000")
    @Max(value = 100_000_000L, message = "singleTransactionLimit must not exceed 100,000,000")
    private long singleTransactionLimit = 20_000_000L;

    /** Daily limit applied to newly created customer wallets (VND). */
    @Min(value = 1_000L, message = "dailyLimit must be at least 1,000")
    @Max(value = 100_000_000L, message = "dailyLimit must not exceed 100,000,000")
    private long dailyLimit = 50_000_000L;

    /** Monthly limit per wallet applied to newly created customer wallets (VND). */
    @Min(value = 1_000L, message = "monthlyLimit must be at least 1,000")
    @Max(value = 100_000_000L, message = "monthlyLimit must not exceed 100,000,000")
    private long monthlyLimit = 100_000_000L;

    /**
     * Legal monthly limit per customer, aggregated across all of the customer's wallets (VND).
     * Consumed by the limit enforcement in a later release.
     */
    @Min(value = 1_000L, message = "customerMonthlyLimit must be at least 1,000")
    @Max(value = 100_000_000L, message = "customerMonthlyLimit must not exceed 100,000,000")
    private long customerMonthlyLimit = 100_000_000L;

    @PostConstruct
    void validateOrder() {
        if (singleTransactionLimit > dailyLimit || dailyLimit > monthlyLimit) {
            throw new IllegalStateException(
                    "Invalid wallet default limits: require singleTransactionLimit <= dailyLimit <= monthlyLimit");
        }
    }
}