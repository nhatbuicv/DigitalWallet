package com.nhatbui.wallet_ledger.dto.response.auth;

import com.nhatbui.wallet_ledger.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UUID uid;
    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private String defaultWalletNumber;
    private Long defaultWalletBalance;
}