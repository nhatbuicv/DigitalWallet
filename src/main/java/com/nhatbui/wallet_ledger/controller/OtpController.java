package com.nhatbui.wallet_ledger.controller;

import com.nhatbui.wallet_ledger.dto.request.auth.SendOtpRequest;
import com.nhatbui.wallet_ledger.dto.request.auth.VerifyOtpRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.service.notification.EmailService;
import com.nhatbui.wallet_ledger.service.auth.OtpService;
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
