package com.nhatbui.wallet_ledger.dto.request.admin;

import com.nhatbui.wallet_ledger.enums.UserRole;
import com.nhatbui.wallet_ledger.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String phone;
    private String email;
    private String fullName;
    private UserStatus status;
    private UserRole role;
}