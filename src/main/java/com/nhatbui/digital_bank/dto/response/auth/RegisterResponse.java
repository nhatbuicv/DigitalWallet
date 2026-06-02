package com.nhatbui.digital_bank.dto.response.auth;

import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private UserProfileResponse user;
    private AccountResponse account;
    private boolean phoneVerified;
}
