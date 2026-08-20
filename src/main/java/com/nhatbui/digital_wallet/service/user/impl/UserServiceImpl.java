package com.nhatbui.digital_wallet.service.user.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.auth.ChangePasswordRequest;
import com.nhatbui.digital_wallet.dto.request.user.UpdateProfileRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.UserMapper;
import com.nhatbui.digital_wallet.repository.ProvinceRepository;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.user.UserService;
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
    private final PasswordEncoder passwordEncoder;

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
    public ApiResponse<Void> changePassword(ChangePasswordRequest request) {
        User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .success(true)
                .build();
    }
}