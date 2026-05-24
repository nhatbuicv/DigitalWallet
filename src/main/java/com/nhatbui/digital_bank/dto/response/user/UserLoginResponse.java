package com.nhatbui.digital_bank.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginResponse {

    // Authentication info
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    // User info
    private UserProfileResponse user;

    // Thông tin account mặc định
    private String defaultAccountNumber;
    private java.math.BigDecimal defaultAccountBalance;
}