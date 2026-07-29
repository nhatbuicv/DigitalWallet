package com.nhatbui.digital_wallet.dto.response.auth;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    // Default wallet
    private String defaultWalletNumber;
    private Long defaultWalletBalance;
}