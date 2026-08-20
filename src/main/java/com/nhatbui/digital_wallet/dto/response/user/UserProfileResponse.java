package com.nhatbui.digital_wallet.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_wallet.dto.response.location.ProvinceResponse;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserProfileResponse {

    private UUID uid;
    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private ProvinceResponse provinceResponse;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String address;
    private String status;
    private String role;
    private Boolean phoneVerified;
    private Boolean walletNonLocked;
    private Integer failedLoginAttempts;

    private Instant lastLogin;
    private Instant createdAt;
    private Instant updatedAt;
}