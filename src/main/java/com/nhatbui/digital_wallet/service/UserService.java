package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.digital_wallet.dto.request.user.UpdateProfileRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    // USER
    ApiResponse<UserProfileResponse> getMyProfile();

    ApiResponse<UserProfileResponse> updateMyProfile(UpdateProfileRequest request);

    ApiResponse<Void> changePassword(ChangePasswordRequest request);

    // ADMIN
    PageResponse<UserAdminResponse> getUsers(Pageable pageable, UserStatus status, UserRole role);

    ApiResponse<UserAdminResponse> getUserDetail(UUID uid);

    ApiResponse<Void> changeUserStatus(UUID uid, UserStatus status);

    ApiResponse<Void> changeUserRole(UUID uid, UserRole role);
}