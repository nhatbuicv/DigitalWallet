package com.nhatbui.digital_bank.dto.response.auth;

import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserLoginResponse {
    // Token
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    // User info
    private String customerId;
    private String fullName;
    private String phone;

    // Default account
    private String defaultAccountNumber;
    private BigDecimal defaultAccountBalance;
}