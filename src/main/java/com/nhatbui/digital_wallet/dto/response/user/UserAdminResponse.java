package com.nhatbui.digital_wallet.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Thông tin thống kê
    private Integer totalWallets;
    private Long totalBalance;
}