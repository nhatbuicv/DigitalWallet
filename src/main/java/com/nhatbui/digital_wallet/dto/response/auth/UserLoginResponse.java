package com.nhatbui.digital_wallet.dto.response.auth;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String customerId;
    private String fullName;
    private String phone;
    private String defaultWalletNumber;
    private Long defaultWalletBalance;
}