package com.nhatbui.wallet_ledger.service.auth;

import com.nhatbui.wallet_ledger.dto.request.user.CreateUserRequest;
import com.nhatbui.wallet_ledger.dto.request.auth.PasswordLoginRequest;
import com.nhatbui.wallet_ledger.dto.request.auth.QuickLoginRequest;
import com.nhatbui.wallet_ledger.dto.request.auth.RefreshTokenRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.device.DeviceSessionResponse;
import com.nhatbui.wallet_ledger.dto.response.auth.RegisterResponse;
import com.nhatbui.wallet_ledger.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.wallet_ledger.dto.response.auth.UserLoginResponse;
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