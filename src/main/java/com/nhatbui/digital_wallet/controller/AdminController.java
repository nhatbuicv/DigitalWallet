package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserAdminResponse;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.service.UserService;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role) {

        PageResponse<UserAdminResponse> data = userService.getUsers(pageable, status, role);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<UserAdminResponse>>builder()
                        .message("Lấy danh sách người dùng thành công")
                        .success(true)
                        .data(data)
                        .build());
    }

    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetail(@PathVariable UUID uid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserDetail(uid));
    }

    @PatchMapping("/{uid}/status")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable UUID uid,
            @RequestParam UserStatus status) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.changeUserStatus(uid, status));
    }

    @PatchMapping("/{uid}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable UUID uid,
            @RequestParam UserRole role) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.changeUserRole(uid, role));
    }
}