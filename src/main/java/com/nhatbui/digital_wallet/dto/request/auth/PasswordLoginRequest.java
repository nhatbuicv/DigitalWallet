package com.nhatbui.digital_wallet.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordLoginRequest {
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Invalid Vietnamese phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Device ID is required")
    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;

    @Size(max = 100, message = "Device name must not exceed 100 characters")
    private String deviceName;
}
