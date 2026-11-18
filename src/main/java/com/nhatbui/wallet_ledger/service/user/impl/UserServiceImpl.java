package com.nhatbui.wallet_ledger.service.user.impl;

import com.nhatbui.wallet_ledger.config.SecurityUtils;
import com.nhatbui.wallet_ledger.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.wallet_ledger.dto.request.user.UpdateProfileRequest;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.dto.response.user.UserProfileResponse;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.mapper.UserMapper;
import com.nhatbui.wallet_ledger.repository.ProvinceRepository;
import com.nhatbui.wallet_ledger.repository.RefreshTokenRepository;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import com.nhatbui.wallet_ledger.service.auth.TokenBlacklistService;
import com.nhatbui.wallet_ledger.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;
    private final ProvinceRepository provinceRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        User user = securityUtils.getCurrentUser();

        UserProfileResponse response = userMapper.mapToUserProfileResponse(user);

        return ApiResponse.<UserProfileResponse>builder()
                .code(1000)
                .message("Lấy hồ sơ thành công")
                .data(response)
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateMyProfile(UpdateProfileRequest request) {
        User user = securityUtils.getCurrentUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getProvinceCode() != null) {
            var province = provinceRepository.findByCode(request.getProvinceCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));
            user.setProvince(province);
        }

        userRepository.save(user);

        return ApiResponse.<UserProfileResponse>builder()
                .message("Cập nhật thông tin thành công")
                .data(userMapper.mapToUserProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(ChangePasswordRequest passwordChangeRequest) {
        User currentUser = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(passwordChangeRequest.getPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        currentUser.setPassword(passwordEncoder.encode(passwordChangeRequest.getPassword()));
        currentUser.setTokenVersion(currentUser.getTokenVersion() + 1);
        userRepository.save(currentUser);
        refreshTokenRepository.deleteByUserUid(currentUser.getUid());
        eventPublisher.publishEvent(new com.nhatbui.wallet_ledger.event.UserSessionRevokedEvent(currentUser.getUid(), null));
        blacklistCurrentAccessToken();

        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .success(true)
                .build();
    }

    private void blacklistCurrentAccessToken() {
        String currentAccessTokenId = securityUtils.getCurrentTokenId();
        var accessTokenExpiresAt = securityUtils.getCurrentTokenExpiresAt();
        if (currentAccessTokenId != null && accessTokenExpiresAt != null) {
            tokenBlacklistService.blacklistToken(currentAccessTokenId, accessTokenExpiresAt);
        }
    }
}