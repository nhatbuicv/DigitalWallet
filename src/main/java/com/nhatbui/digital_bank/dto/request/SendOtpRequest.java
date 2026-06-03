package com.nhatbui.digital_bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Invalid Vietnamese phone number")
    private String phone;
}