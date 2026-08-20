package com.nhatbui.digital_wallet.service.admin;

import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.UserMapper;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.admin.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserMapper userMapper;

    private AdminUserServiceImpl adminUserService;

    private static final UUID USER_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private User testUser;
    private UserAdminResponse testUserResponse;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository, walletRepository, userMapper);

        testUser = User.builder()
                .uid(USER_UID)
                .fullName("Test User")
                .email("test@example.com")
                .phone("0912345678")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        testUserResponse = UserAdminResponse.builder()
                .uid(USER_UID.toString())
                .fullName("Test User")
                .email("test@example.com")
                .phone("0912345678")
                .status("ACTIVE")
                .role("USER")
                .totalWallets(1)
                .totalBalance(100000L)
                .build();
    }

    // ========== getUsers ==========

    @Test
    void getUsers_withNoFilters_shouldReturnAllUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder().build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getFullName()).isEqualTo("Test User");

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withPhoneFilter_shouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder().phone("0912345678").build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withEmailFilter_shouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder().email("test@example.com").build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withStatusFilter_shouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder().status(UserStatus.ACTIVE).build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withRoleFilter_shouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder().role(UserRole.USER).build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withAllFilters_shouldReturnFilteredUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .phone("0912345678")
                .email("test@example.com")
                .fullName("Test")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUsers_withEmptyResult_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        UserSearchRequest searchRequest = UserSearchRequest.builder().phone("nonexistent").build();
        PageResponse<UserAdminResponse> result = adminUserService.getUsers(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getData()).isEmpty();
    }

    // ========== getUserDetail ==========

    @Test
    void getUserDetail_withExistingUid_shouldReturnUser() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(testUser));
        when(userMapper.mapToUserAdminResponse(testUser)).thenReturn(testUserResponse);
        when(walletRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(Wallet.builder().balance(100000L).build()));

        ApiResponse<UserAdminResponse> result = adminUserService.getUserDetail(USER_UID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getFullName()).isEqualTo("Test User");
        assertThat(result.getData().getTotalWallets()).isEqualTo(1);
        assertThat(result.getData().getTotalBalance()).isEqualTo(100000L);
    }

    @Test
    void getUserDetail_withNonExistingUid_shouldThrowException() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserDetail(USER_UID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    // ========== changeUserStatus ==========

    @Test
    void changeUserStatus_withExistingUid_shouldUpdateStatus() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(testUser));

        ApiResponse<Void> result = adminUserService.changeUserStatus(USER_UID, UserStatus.LOCKED);

        assertThat(result.isSuccess()).isTrue();
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserStatus_withNonExistingUid_shouldThrowException() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changeUserStatus(USER_UID, UserStatus.LOCKED))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    // ========== changeUserRole ==========

    @Test
    void changeUserRole_withExistingUid_shouldUpdateRole() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.of(testUser));

        ApiResponse<Void> result = adminUserService.changeUserRole(USER_UID, UserRole.MANAGER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(testUser.getRole()).isEqualTo(UserRole.MANAGER);
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserRole_withNonExistingUid_shouldThrowException() {
        when(userRepository.findByUid(USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changeUserRole(USER_UID, UserRole.MANAGER))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}