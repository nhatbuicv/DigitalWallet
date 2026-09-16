package com.nhatbui.digital_wallet.service.admin.impl;

import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.UserMapper;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.repository.specification.UserSpecification;
import com.nhatbui.digital_wallet.service.admin.AdminUserService;
import com.nhatbui.digital_wallet.service.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserAdminResponse> getUsers(Pageable pageable, UserSearchRequest searchRequest) {
        Specification<User> spec = UserSpecification.buildSearchSpec(searchRequest);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        return PageResponse.from(userPage, this::toAdminResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserAdminResponse> getUserDetail(UUID uid) {
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ApiResponse.<UserAdminResponse>builder()
                .message("Lấy thông tin người dùng thành công")
                .success(true)
                .data(toAdminResponse(user))
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> changeUserStatus(UUID uid, UserStatus status) {
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserStatus previousStatus = user.getStatus();
        user.setStatus(status);
        userRepository.save(user);

        auditService.log(
                AuditAction.USER_STATUS_CHANGE,
                AuditTargetType.USER,
                user.getUid(),
                user.getEmail(),
                String.format("Thay đổi trạng thái người dùng từ %s thành %s", previousStatus, status));

        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái người dùng thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> changeUserRole(UUID uid, UserRole role) {
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserRole previousRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);

        auditService.log(
                AuditAction.USER_ROLE_CHANGE,
                AuditTargetType.USER,
                user.getUid(),
                user.getEmail(),
                String.format("Thay đổi vai trò người dùng từ %s thành %s", previousRole, role));

        return ApiResponse.<Void>builder()
                .message("Cập nhật vai trò người dùng thành công")
                .success(true)
                .build();
    }

    private UserAdminResponse toAdminResponse(User user) {
        UserAdminResponse response = userMapper.mapToUserAdminResponse(user);

        List<Wallet> wallets = walletRepository.findAllByUserUid(user.getUid());
        response.setTotalWallets(wallets.size());
        response.setTotalBalance(wallets.stream()
                .mapToLong(Wallet::getBalance)
                .sum());

        return response;
    }
}