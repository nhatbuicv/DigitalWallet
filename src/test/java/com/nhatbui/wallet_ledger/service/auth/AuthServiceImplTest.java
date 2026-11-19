package com.nhatbui.wallet_ledger.service.auth;

import com.nhatbui.wallet_ledger.config.SecurityUtils;
import com.nhatbui.wallet_ledger.dto.request.auth.PasswordLoginRequest;
import com.nhatbui.wallet_ledger.dto.request.auth.QuickLoginRequest;
import com.nhatbui.wallet_ledger.dto.response.auth.UserLoginResponse;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.entity.DeviceSession;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.ClientType;
import com.nhatbui.wallet_ledger.enums.UserRole;
import com.nhatbui.wallet_ledger.enums.UserStatus;
import com.nhatbui.wallet_ledger.enums.WalletType;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.generator.CustomerIdGenerator;
import com.nhatbui.wallet_ledger.mapper.UserMapper;
import com.nhatbui.wallet_ledger.mapper.WalletMapper;
import com.nhatbui.wallet_ledger.repository.DeviceSessionRepository;
import com.nhatbui.wallet_ledger.repository.PasswordResetTokenRepository;
import com.nhatbui.wallet_ledger.repository.ProvinceRepository;
import com.nhatbui.wallet_ledger.repository.RefreshTokenRepository;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import com.nhatbui.wallet_ledger.repository.WalletRepository;
import com.nhatbui.wallet_ledger.service.auth.impl.AuthServiceImpl;
import com.nhatbui.wallet_ledger.service.notification.EmailService;
import com.nhatbui.wallet_ledger.service.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TEST_SIGNER_KEY = "test-dummy-signer-key-must-be-at-least-32-bytes-long-0123456789";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ProvinceRepository provinceRepository;
    @Mock
    private CustomerIdGenerator customerIdGenerator;
    @Mock
    private WalletService walletService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private WalletMapper walletMapper;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private DeviceSessionRepository deviceSessionRepository;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                provinceRepository,
                customerIdGenerator,
                walletService,
                walletRepository,
                userMapper,
                walletMapper,
                refreshTokenRepository,
                loginAttemptService,
                deviceSessionRepository,
                walletService,
                rateLimitService,
                passwordResetTokenRepository,
                emailService,
                securityUtils,
                tokenBlacklistService,
                eventPublisher
        );
        ReflectionTestUtils.setField(authService, "signerKey", TEST_SIGNER_KEY);
        ReflectionTestUtils.setField(authService, "accessTokenDuration", 3600L);
        ReflectionTestUtils.setField(authService, "refreshTokenDuration", 86400L);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ADMIN", "MANAGER", "STAFF"})
    void loginWithPassword_backofficeActiveUser_shouldReturnUidEmailAndRole(UserRole role) {
        UUID uid = UUID.randomUUID();
        String email = role.name().toLowerCase() + "@wallet.local";
        User user = User.builder()
                .uid(uid)
                .customerId(role.name().substring(0, 3) + "001")
                .fullName("Test " + role.name())
                .email(email)
                .password("encoded-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .walletNonLocked(true)
                .tokenVersion(0)
                .build();

        PasswordLoginRequest request = buildWebAdminLoginRequest(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);
        when(deviceSessionRepository.findByDeviceId("web-device-1")).thenReturn(Optional.empty());

        ApiResponse<UserLoginResponse> response = authService.loginWithPassword(request);

        assertThat(response.isSuccess()).isTrue();
        UserLoginResponse data = response.getData();
        assertThat(data).isNotNull();
        assertThat(data.getUid()).isEqualTo(uid);
        assertThat(data.getEmail()).isEqualTo(email);
        assertThat(data.getRole()).isEqualTo(role);
        assertThat(data.getCustomerId()).isEqualTo(user.getCustomerId());
        assertThat(data.getFullName()).isEqualTo(user.getFullName());
        assertThat(data.getAccessToken()).isNotBlank();
        assertThat(data.getRefreshToken()).isNotBlank();
        assertThat(data.getTokenType()).isEqualTo("Bearer");
        assertThat(data.getExpiresIn()).isEqualTo(3600L);
        assertThat(data.getDefaultWalletNumber()).isNull();
        assertThat(data.getDefaultWalletBalance()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = {"SUSPENDED", "LOCKED", "INACTIVE"})
    void loginWithPassword_staffWithBlockedStatus_shouldThrowUserInactive(UserStatus blockedStatus) {
        User staff = User.builder()
                .uid(UUID.randomUUID())
                .email("staff@wallet.local")
                .password("encoded-password")
                .role(UserRole.STAFF)
                .status(blockedStatus)
                .walletNonLocked(true)
                .build();

        PasswordLoginRequest request = buildWebAdminLoginRequest("staff@wallet.local");
        when(userRepository.findByEmail("staff@wallet.local")).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> authService.loginWithPassword(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_INACTIVE));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginWithPassword_managerBruteForceLockedNotExpired_shouldThrowUserLocked() {
        User manager = User.builder()
                .uid(UUID.randomUUID())
                .email("manager@wallet.local")
                .password("encoded-password")
                .role(UserRole.MANAGER)
                .status(UserStatus.ACTIVE)
                .walletNonLocked(false)
                .lockTime(Instant.now().minusSeconds(60))
                .failedLoginAttempts(5)
                .build();

        PasswordLoginRequest request = buildWebAdminLoginRequest("manager@wallet.local");
        when(userRepository.findByEmail("manager@wallet.local")).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> authService.loginWithPassword(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_LOCKED));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginWithPassword_managerBruteForceLockExpired_shouldAutoUnlockAndSucceed() {
        UUID uid = UUID.randomUUID();
        User manager = User.builder()
                .uid(uid)
                .customerId("MGR001")
                .fullName("Manager User")
                .email("manager@wallet.local")
                .password("encoded-password")
                .role(UserRole.MANAGER)
                .status(UserStatus.ACTIVE)
                .walletNonLocked(false)
                .lockTime(Instant.now().minusSeconds(1801))
                .failedLoginAttempts(5)
                .tokenVersion(0)
                .build();

        PasswordLoginRequest request = buildWebAdminLoginRequest("manager@wallet.local");
        when(userRepository.findByEmail("manager@wallet.local")).thenReturn(Optional.of(manager));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);
        when(deviceSessionRepository.findByDeviceId("web-device-1")).thenReturn(Optional.empty());

        ApiResponse<UserLoginResponse> response = authService.loginWithPassword(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(manager.getWalletNonLocked()).isTrue();
        assertThat(manager.getLockTime()).isNull();
        assertThat(manager.getFailedLoginAttempts()).isZero();
        assertThat(response.getData().getUid()).isEqualTo(uid);
        assertThat(response.getData().getRole()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    void loginWithPassword_mobileUserSuspended_shouldThrowUserInactive() {
        User customer = User.builder()
                .uid(UUID.randomUUID())
                .phone("0912345678")
                .email("customer@wallet.local")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.SUSPENDED)
                .walletNonLocked(true)
                .build();

        PasswordLoginRequest request = new PasswordLoginRequest();
        request.setIdentifier("0912345678");
        request.setClientType(ClientType.MOBILE);
        request.setPassword("Password123!");
        request.setDeviceId("mobile-1");

        when(userRepository.findByPhone("0912345678")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> authService.loginWithPassword(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_INACTIVE));
    }

    @Test
    void loginWithPassword_mobileUserPending_shouldThrowPhoneNotVerified() {
        User customer = User.builder()
                .uid(UUID.randomUUID())
                .phone("0912345678")
                .email("customer@wallet.local")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .walletNonLocked(true)
                .build();

        PasswordLoginRequest request = new PasswordLoginRequest();
        request.setIdentifier("0912345678");
        request.setClientType(ClientType.MOBILE);
        request.setPassword("Password123!");
        request.setDeviceId("mobile-1");

        when(userRepository.findByPhone("0912345678")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> authService.loginWithPassword(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PHONE_NOT_VERIFIED));
    }

    @Test
    void quickLogin_activeCustomer_shouldReturnUidEmailRoleAndDefaultWallet() {
        UUID uid = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .walletNumber("WL000001")
                .walletType(WalletType.PERSONAL)
                .availableBalance(500000L)
                .build();
        User customer = User.builder()
                .uid(uid)
                .customerId("CUS001")
                .fullName("Customer One")
                .email("customer@wallet.local")
                .phone("0912345678")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .walletNonLocked(true)
                .tokenVersion(0)
                .wallets(List.of(wallet))
                .build();
        DeviceSession session = DeviceSession.builder()
                .deviceId("mobile-1")
                .deviceName("iPhone")
                .user(customer)
                .build();

        QuickLoginRequest request = new QuickLoginRequest();
        request.setDeviceId("mobile-1");
        request.setPassword("Password123!");

        when(deviceSessionRepository.findByDeviceId("mobile-1")).thenReturn(Optional.of(session));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);

        ApiResponse<UserLoginResponse> response = authService.quickLogin(request);

        assertThat(response.isSuccess()).isTrue();
        UserLoginResponse data = response.getData();
        assertThat(data.getUid()).isEqualTo(uid);
        assertThat(data.getEmail()).isEqualTo("customer@wallet.local");
        assertThat(data.getRole()).isEqualTo(UserRole.USER);
        assertThat(data.getDefaultWalletNumber()).isEqualTo("WL000001");
        assertThat(data.getDefaultWalletBalance()).isEqualTo(500000L);
    }

    private PasswordLoginRequest buildWebAdminLoginRequest(String email) {
        PasswordLoginRequest request = new PasswordLoginRequest();
        request.setIdentifier(email);
        request.setClientType(ClientType.WEB_ADMIN);
        request.setPassword("Password123!");
        request.setDeviceId("web-device-1");
        request.setDeviceName("Chrome");
        return request;
    }
}
