package com.nhatbui.digital_wallet.dto.response;

import com.nhatbui.digital_wallet.enums.CurrencyCode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceResponse {

    private String accountNumber;

    private CurrencyCode currency;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private BigDecimal blockedBalance;
}