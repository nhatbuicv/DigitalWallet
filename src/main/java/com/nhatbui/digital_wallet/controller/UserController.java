package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.digital_wallet.dto.request.user.UpdateProfileRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateMyProfile(request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.changePassword(request));
    }
}