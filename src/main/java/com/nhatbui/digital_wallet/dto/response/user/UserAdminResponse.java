package com.nhatbui.digital_wallet.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
public class UserAdminResponse {

    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String citizenId;
    private String dateOfBirth;
    private String address;
    private String status;
    private String role;
    private Boolean phoneVerified;
    private Boolean walletNonLocked;
    private Integer failedLoginAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant lockTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    // Thông tin thống kê
    private Integer totalWallets;
    private Long totalBalance;
}