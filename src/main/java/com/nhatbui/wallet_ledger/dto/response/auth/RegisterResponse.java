package com.nhatbui.wallet_ledger.dto.response.auth;

import com.nhatbui.wallet_ledger.dto.response.wallet.WalletResponse;
import com.nhatbui.wallet_ledger.dto.response.user.UserProfileResponse;
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
