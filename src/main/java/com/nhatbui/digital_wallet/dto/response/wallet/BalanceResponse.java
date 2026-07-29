package com.nhatbui.digital_wallet.dto.response.wallet;

import com.nhatbui.digital_wallet.enums.CurrencyCode;
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