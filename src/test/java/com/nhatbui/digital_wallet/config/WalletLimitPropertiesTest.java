package com.nhatbui.digital_wallet.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletLimitPropertiesTest {

    @Test
    void defaults_matchPolicyValues() {
        WalletLimitProperties properties = new WalletLimitProperties();

        assertThat(properties.getSingleTransactionLimit()).isEqualTo(20_000_000L);
        assertThat(properties.getDailyLimit()).isEqualTo(50_000_000L);
        assertThat(properties.getMonthlyLimit()).isEqualTo(100_000_000L);
        assertThat(properties.getCustomerMonthlyLimit()).isEqualTo(100_000_000L);
    }

    @Test
    void validateOrder_acceptsAscendingLimits() {
        WalletLimitProperties properties = new WalletLimitProperties();
        properties.setSingleTransactionLimit(1_000L);
        properties.setDailyLimit(100_000_000L);
        properties.setMonthlyLimit(100_000_000L);

        assertThatCode(properties::validateOrder).doesNotThrowAnyException();
    }

    @Test
    void validateOrder_rejectsSingleLimitGreaterThanDailyLimit() {
        WalletLimitProperties properties = new WalletLimitProperties();
        properties.setSingleTransactionLimit(60_000_000L);
        properties.setDailyLimit(50_000_000L);

        assertThatThrownBy(properties::validateOrder)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("singleTransactionLimit <= dailyLimit <= monthlyLimit");
    }

    @Test
    void validateOrder_rejectsDailyLimitGreaterThanMonthlyLimit() {
        WalletLimitProperties properties = new WalletLimitProperties();
        properties.setDailyLimit(50_000_000L);
        properties.setMonthlyLimit(40_000_000L);

        assertThatThrownBy(properties::validateOrder)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("singleTransactionLimit <= dailyLimit <= monthlyLimit");
    }
}
