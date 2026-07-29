package com.nhatbui.digital_wallet.dto.response.auth;

import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private UserProfileResponse user;
    private WalletResponse wallet;
    private boolean phoneVerified;
}
