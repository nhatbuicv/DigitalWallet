package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.auth.*;
import com.nhatbui.digital_wallet.dto.request.user.CreateUserRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.device.DeviceSessionResponse;
import com.nhatbui.digital_wallet.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_wallet.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.digital_wallet.dto.response.auth.UserLoginResponse;
import com.nhatbui.digital_wallet.service.auth.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        ApiResponse<RegisterResponse> response = authService.registerUserWithDefaultWallet(request);
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> response = authService.logout(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> response = authService.logoutAll(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/device-session/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceSession(@PathVariable String deviceId) {
        ApiResponse<Void> response = authService.deleteDeviceSession(deviceId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        ApiResponse<Void> response = authService.requestPasswordReset(request.getEmail(), getClientIp(httpRequest));
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        ApiResponse<Void> response = authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}