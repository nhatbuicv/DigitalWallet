package com.nhatbui.wallet_ledger.service.user;

import com.nhatbui.wallet_ledger.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.wallet_ledger.dto.request.user.UpdateProfileRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.user.UserProfileResponse;

public interface UserService {
    ApiResponse<UserProfileResponse> getMyProfile();

    ApiResponse<UserProfileResponse> updateMyProfile(UpdateProfileRequest request);

    ApiResponse<Void> changePassword(ChangePasswordRequest request);
}
