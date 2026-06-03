package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_bank.dto.request.QuickLoginRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.DeviceSessionResponse;
import com.nhatbui.digital_bank.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.auth.UserLoginResponse;

public interface  AuthService {

    ApiResponse<RegisterResponse> registerUserWithDefaultAccount(
            CreateUserRequest request);

    ApiResponse<UserLoginResponse> loginWithPassword(
            PasswordLoginRequest request);

    ApiResponse<UserLoginResponse> quickLogin(
            QuickLoginRequest request);

    ApiResponse<DeviceSessionResponse> getDeviceSession(
            String deviceId);


}