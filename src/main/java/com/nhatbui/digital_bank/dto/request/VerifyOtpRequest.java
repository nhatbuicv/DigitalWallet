package com.nhatbui.digital_bank.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String phone;
    private String otp;
}