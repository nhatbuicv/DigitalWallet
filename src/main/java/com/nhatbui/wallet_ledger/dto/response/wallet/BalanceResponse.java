package com.nhatbui.wallet_ledger.dto.response.wallet;

import com.nhatbui.wallet_ledger.enums.CurrencyCode;
import lombok.*;


@Getter
@Setter
@Builder
public class BalanceResponse {
    private String walletNumber;
    private CurrencyCode currency;
    private Long balance;
    private Long availableBalance;
    private Long blockedBalance;
}