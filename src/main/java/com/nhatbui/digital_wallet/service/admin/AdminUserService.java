package com.nhatbui.digital_wallet.service.admin;

import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    PageResponse<UserAdminResponse> getUsers(Pageable pageable, UserSearchRequest searchRequest);

    ApiResponse<UserAdminResponse> getUserDetail(UUID uid);

    ApiResponse<Void> changeUserStatus(UUID uid, UserStatus status);

    ApiResponse<Void> changeUserRole(UUID uid, UserRole role);
}