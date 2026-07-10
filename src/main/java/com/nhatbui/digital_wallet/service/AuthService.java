package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.user.CreateUserRequest;
import com.nhatbui.digital_wallet.dto.request.auth.PasswordLoginRequest;
import com.nhatbui.digital_wallet.dto.request.auth.QuickLoginRequest;
import com.nhatbui.digital_wallet.dto.request.auth.RefreshTokenRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.device.DeviceSessionResponse;
import com.nhatbui.digital_wallet.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_wallet.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.digital_wallet.dto.response.auth.UserLoginResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface  AuthService {
    ApiResponse<RegisterResponse> registerUserWithDefaultWallet(CreateUserRequest request);

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