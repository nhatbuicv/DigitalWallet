package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role) {

        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .phone(phone)
                .email(email)
                .fullName(fullName)
                .status(status)
                .role(role)
                .build();

        PageResponse<UserAdminResponse> data = adminUserService.getUsers(pageable, searchRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<UserAdminResponse>>builder()
                        .message("Lấy danh sách người dùng thành công")
                        .success(true)
                        .data(data)
                        .build());
    }

    @GetMapping("/{uid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetail(@PathVariable UUID uid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminUserService.getUserDetail(uid));
    }

    @PatchMapping("/{uid}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable UUID uid,
            @RequestParam UserStatus status) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminUserService.changeUserStatus(uid, status));
    }

    @PatchMapping("/{uid}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable UUID uid,
            @RequestParam UserRole role) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(adminUserService.changeUserRole(uid, role));
    }
}