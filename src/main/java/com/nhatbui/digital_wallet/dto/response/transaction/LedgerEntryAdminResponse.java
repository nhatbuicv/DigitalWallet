package com.nhatbui.digital_wallet.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_wallet.enums.EntryType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryAdminResponse {
    private String id;
    private String walletId;
    private String walletNumber;
    private EntryType entryType;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
}
