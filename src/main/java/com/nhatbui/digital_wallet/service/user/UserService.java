package com.nhatbui.digital_wallet.service.user;

import com.nhatbui.digital_wallet.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.digital_wallet.dto.request.user.UpdateProfileRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;

public interface UserService {
    ApiResponse<UserProfileResponse> getMyProfile();

    ApiResponse<UserProfileResponse> updateMyProfile(UpdateProfileRequest request);

    ApiResponse<Void> changePassword(ChangePasswordRequest request);
}
