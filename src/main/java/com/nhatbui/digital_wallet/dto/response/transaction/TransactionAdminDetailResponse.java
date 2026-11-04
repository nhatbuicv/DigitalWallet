package com.nhatbui.digital_wallet.dto.response.transaction;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAdminDetailResponse extends TransactionAdminResponse {

    private String idempotencyKey;

    @Builder.Default
    private List<LedgerEntryAdminResponse> ledgerEntries = new ArrayList<>();
}
