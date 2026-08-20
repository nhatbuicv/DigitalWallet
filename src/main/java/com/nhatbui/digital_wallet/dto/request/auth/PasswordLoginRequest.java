package com.nhatbui.digital_wallet.dto.request.auth;

import com.nhatbui.digital_wallet.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordLoginRequest {
    @NotBlank(message = "Identifier is required")
    @Size(max = 100, message = "Identifier must not exceed 100 characters")
    private String identifier;

    @NotNull(message = "Client type is required")
    private ClientType clientType;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Device ID is required")
    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;

    @Size(max = 100, message = "Device name must not exceed 100 characters")
    private String deviceName;
}