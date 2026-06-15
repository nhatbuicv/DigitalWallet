package com.nhatbui.digital_wallet.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceSessionResponse {
    private String fullName;
    private String phone;
    private boolean quickLoginEnabled;
}