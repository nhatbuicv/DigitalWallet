package com.nhatbui.digital_bank.dto.response;

import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_bank.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private UserProfileResponse user;
    private AccountResponse account;
    private boolean emailVerified;
    private boolean phoneVerified;
}
