package com.nhatbui.wallet_ledger.service.admin;

import com.nhatbui.wallet_ledger.dto.request.admin.UserSearchRequest;
import com.nhatbui.wallet_ledger.dto.response.admin.UserAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import com.nhatbui.wallet_ledger.enums.UserRole;
import com.nhatbui.wallet_ledger.enums.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    PageResponse<UserAdminResponse> getUsers(Pageable pageable, UserSearchRequest searchRequest);

    ApiResponse<UserAdminResponse> getUserDetail(UUID uid);

    ApiResponse<Void> changeUserStatus(UUID uid, UserStatus status);

    ApiResponse<Void> changeUserRole(UUID uid, UserRole role);
}