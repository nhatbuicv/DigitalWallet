package com.nhatbui.digital_bank.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordLoginRequest {
    private String phone;
    private String password;
    private String deviceId;
    private String deviceName;
}
