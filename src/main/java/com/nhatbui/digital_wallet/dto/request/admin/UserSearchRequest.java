package com.nhatbui.digital_wallet.dto.request.admin;

import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
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