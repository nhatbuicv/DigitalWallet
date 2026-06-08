package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_bank.dto.request.QuickLoginRequest;
import com.nhatbui.digital_bank.dto.request.RefreshTokenRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.DeviceSessionResponse;
import com.nhatbui.digital_bank.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.digital_bank.dto.response.auth.UserLoginResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface  AuthService {
    ApiResponse<RegisterResponse> registerUserWithDefaultAccount(CreateUserRequest request);

    ApiResponse<UserLoginResponse> loginWithPassword(PasswordLoginRequest request);

    ApiResponse<UserLoginResponse> quickLogin(QuickLoginRequest request);

    ApiResponse<DeviceSessionResponse> getDeviceSession(String deviceId);

    ApiResponse<TokenRefreshResponse> refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;

    ApiResponse<Void> logout(RefreshTokenRequest request) throws ParseException, JOSEException;

    ApiResponse<Void> logoutAll(RefreshTokenRequest request) throws ParseException, JOSEException;

    ApiResponse<Void> deleteDeviceSession(String deviceId);

    ApiResponse<Void> requestPasswordReset(String email, String ipAddress);

    ApiResponse<Void> resetPassword(String rawToken, String newPassword);

}