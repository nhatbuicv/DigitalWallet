package com.nhatbui.digital_bank.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuickLoginRequest {
    private String deviceId;
    private String password;
}