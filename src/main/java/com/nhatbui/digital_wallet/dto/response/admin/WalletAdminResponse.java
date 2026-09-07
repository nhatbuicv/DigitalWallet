package com.nhatbui.digital_wallet.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
public class WalletAdminResponse {

    private String uid;
    private String walletNumber;
    private String walletName;
    private String walletType;
    private String currency;

    private Long balance;
    private Long availableBalance;
    private Long blockedBalance;

    private Long overdraftLimit;
    private Long minimumBalance;
    private Long dailyLimit;
    private Long monthlyLimit;
    private Long singleTransactionLimit;

    private String status;
    private String branchCode;
    private String branchName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant openedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant closedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    private String userUid;
    private String userFullName;
    private String userPhone;
    private String userEmail;
}