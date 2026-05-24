package com.nhatbui.digital_bank.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_bank.dto.ProvinceResponse;
import com.nhatbui.digital_bank.entity.Province;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {

    private UUID uid;
    private String customerId;
    private String fullName;
    private String email;
    private String phone;

    // Thông tin cá nhân (chỉ user mới thấy)
    private ProvinceResponse provinceResponse;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String address;

    // Trạng thái tài khoản
    private String status;
    private String role;

    // Xác thực
    private Boolean emailVerified;
    private Boolean phoneVerified;

    // Thông tin bảo mật
    private Boolean accountNonLocked;
    private Integer failedLoginAttempts;

    private Instant lastLogin;
    private Instant createdAt;
    private Instant updatedAt;
}