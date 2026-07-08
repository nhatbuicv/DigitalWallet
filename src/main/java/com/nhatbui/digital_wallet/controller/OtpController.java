package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.SendOtpRequest;
import com.nhatbui.digital_wallet.dto.request.VerifyOtpRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.service.EmailService;
import com.nhatbui.digital_wallet.service.impl.EmailServiceImpl;
import com.nhatbui.digital_wallet.service.impl.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final EmailService emailService;

    @PostMapping("/send")
    public ApiResponse<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        emailService.sendToEmail(request.getEmail());

        return ApiResponse.<Void>builder()
                .message("OTP sent successfully")
                .success(true)
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (!valid) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        otpService.activateUser(request.getEmail());

        return ApiResponse.<Void>builder()
                .message("OTP verified successfully")
                .success(true)
                .build();
    }
}
