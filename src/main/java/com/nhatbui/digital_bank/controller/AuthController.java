package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_bank.dto.request.QuickLoginRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.DeviceSessionResponse;
import com.nhatbui.digital_bank.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.auth.UserLoginResponse;
import com.nhatbui.digital_bank.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API xác thực và đăng ký người dùng")
public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        ApiResponse<RegisterResponse> response = authService.registerUserWithDefaultAccount(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@Valid @RequestBody PasswordLoginRequest request) {
        ApiResponse<UserLoginResponse> response = authService.loginWithPassword(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/device-session/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceSessionResponse>> getDeviceSession(@PathVariable String deviceId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.getDeviceSession(deviceId));
    }

    @PostMapping("/quick-login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> quickLogin(@Valid @RequestBody QuickLoginRequest request) {
        ApiResponse<UserLoginResponse> response = authService.quickLogin(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}