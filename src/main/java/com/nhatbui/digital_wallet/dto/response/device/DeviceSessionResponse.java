package com.nhatbui.digital_wallet.dto.response.device;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeviceSessionResponse {
    private String fullName;
    private String phone;
    private boolean quickLoginEnabled;
}