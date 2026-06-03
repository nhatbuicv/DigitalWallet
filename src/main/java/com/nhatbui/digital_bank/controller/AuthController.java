package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_bank.dto.request.QuickLoginRequest;
import com.nhatbui.digital_bank.dto.request.RefreshTokenRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.DeviceSessionResponse;
import com.nhatbui.digital_bank.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.digital_bank.dto.response.auth.UserLoginResponse;
import com.nhatbui.digital_bank.service.AuthService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        ApiResponse<TokenRefreshResponse> response = authService.refreshToken(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> response = authService.logout(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> response = authService.logoutAll(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/device-session/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceSession(@PathVariable String deviceId) {
        ApiResponse<Void> response = authService.deleteDeviceSession(deviceId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}