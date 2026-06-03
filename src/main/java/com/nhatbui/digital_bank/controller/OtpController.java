package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.dto.request.SendOtpRequest;
import com.nhatbui.digital_bank.dto.request.VerifyOtpRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ApiResponse<Void> sendOtp(@RequestBody SendOtpRequest request) {

        String otp = otpService.generateOtp(request.getPhone());

        System.out.println("OTP = " + otp);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("OTP sent successfully")
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyOtp(@RequestBody VerifyOtpRequest request) {

        boolean valid = otpService.verifyOtp(request.getPhone(), request.getOtp());

        if (!valid) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        otpService.activateUser(request.getPhone());

        return ApiResponse.<Void>builder()
                .success(true)
                .message("OTP verified successfully")
                .build();
    }
}
