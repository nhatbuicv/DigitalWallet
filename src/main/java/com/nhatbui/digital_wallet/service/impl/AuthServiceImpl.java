package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.dto.request.CreateUserRequest;
import com.nhatbui.digital_wallet.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_wallet.dto.request.QuickLoginRequest;
import com.nhatbui.digital_wallet.dto.request.RefreshTokenRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.DeviceSessionResponse;
import com.nhatbui.digital_wallet.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_wallet.dto.response.auth.TokenRefreshResponse;
import com.nhatbui.digital_wallet.dto.response.auth.UserLoginResponse;
import com.nhatbui.digital_wallet.entity.*;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.generator.CustomerIdGenerator;
import com.nhatbui.digital_wallet.mapper.WalletMapper;
import com.nhatbui.digital_wallet.mapper.UserMapper;
import com.nhatbui.digital_wallet.repository.*;
import com.nhatbui.digital_wallet.service.WalletService;
import com.nhatbui.digital_wallet.service.AuthService;
import com.nhatbui.digital_wallet.service.LoginAttemptService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final long LOCK_DURATION_SECONDS = 1800;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProvinceRepository provinceRepository;
    private final CustomerIdGenerator customerIdGenerator;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final DeviceSessionRepository deviceSessionRepository;
    private final WalletServiceImpl accountServiceImpl;
    private final RateLimitServiceImpl rateLimitServiceImpl;
    private final PassWordResetTokenRepository passWordResetTokenRepository;
    private final EmailServiceImpl emailServiceImpl;

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Value("${jwt.access-token-duration}")
    private long accessTokenDuration;

    @Value("${jwt.refresh-token-duration}")
    private long refreshTokenDuration;


    // Public API
    @Override
    @Transactional
    public ApiResponse<RegisterResponse> registerUserWithDefaultAccount(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.REGISTRATION_FAILED);
        }

        Province province = provinceRepository.findByCode(request.getCityCode())
                .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));

        User user = buildNewUser(request, province);
        User savedUser = userRepository.save(user);

        Wallet defaultWallet = accountServiceImpl.createDefaultAccount(savedUser);
        Wallet savedWallet = walletRepository.save(defaultWallet);

        RegisterResponse registerResponse = RegisterResponse.builder()
                .user(userMapper.mapToUserProfileResponse(savedUser))
                .account(walletMapper.mapToAccountResponse(savedWallet))
                .build();

        return ApiResponse.<RegisterResponse>builder()
                .message("Đăng ký thành công")
                .success(true)
                .data(registerResponse)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserLoginResponse> loginWithPassword(PasswordLoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        checkAccountStatus(user);
        verifyPassword(request.getPassword(), user);

        refreshTokenRepository.deleteByUserUidAndDeviceId(user.getUid(), request.getDeviceId());
        upsertDeviceSession(user, request.getDeviceId(), request.getDeviceName());

        return buildLoginResponse(user, request.getDeviceId(), request.getDeviceName());
    }

    @Override
    @Transactional
    public ApiResponse<UserLoginResponse> quickLogin(QuickLoginRequest request) {
        DeviceSession session = deviceSessionRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_REGISTERED));

        User user = session.getUser();
        checkAccountStatus(user);
        verifyPassword(request.getPassword(), user);

        refreshTokenRepository.deleteByUserUidAndDeviceId(user.getUid(), request.getDeviceId());

        session.setLastLoginAt(Instant.now());
        deviceSessionRepository.save(session);


        return buildLoginResponse(user, request.getDeviceId(), session.getDeviceName());
    }

    @Override
    public ApiResponse<DeviceSessionResponse> getDeviceSession(String deviceId) {
        DeviceSession session = deviceSessionRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_REGISTERED));

        User user = session.getUser();

        return ApiResponse.<DeviceSessionResponse>builder()
                .success(true)
                .data(DeviceSessionResponse.builder()
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .quickLoginEnabled(true)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<TokenRefreshResponse> refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getRefreshToken(), "refresh");

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        String userId = signedJWT.getJWTClaimsSet().getSubject();

        RefreshToken oldRefreshToken = refreshTokenRepository.findById(UUID.fromString(jit))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(oldRefreshToken);

        String accessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user, oldRefreshToken.getDeviceId(), oldRefreshToken.getDeviceName());

        return ApiResponse.<TokenRefreshResponse>builder()
                .message("Token refreshed successfully")
                .success(true)
                .data(TokenRefreshResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(accessTokenDuration)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> logout(RefreshTokenRequest request) throws ParseException, JOSEException {
            var signedJWT = verifyToken(request.getRefreshToken(), "refresh");
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            refreshTokenRepository.deleteById(UUID.fromString(jti));

        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> logoutAll(RefreshTokenRequest request) throws ParseException, JOSEException {
            var signedJWT = verifyToken(request.getRefreshToken(), "refresh");
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            refreshTokenRepository.deleteByUserUid(UUID.fromString(userId));

        return ApiResponse.<Void>builder()
                .message("Đăng xuất tất cả thiết bị thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteDeviceSession(String deviceId) {
        deviceSessionRepository.findByDeviceId(deviceId).ifPresent(session -> {
            refreshTokenRepository.deleteByUserUidAndDeviceId(session.getUser().getUid(), deviceId);
            deviceSessionRepository.delete(session);
        });

        return ApiResponse.<Void>builder()
                .message("Xóa thiết bị thành công")
                .success(true)
                .build();
    }

    @Override
    public ApiResponse<Void> requestPasswordReset(String email, String ipAddress) {
        rateLimitServiceImpl.checkResetLimit(ipAddress);

        userRepository.findByEmail(email).ifPresent(user -> {
            passWordResetTokenRepository.deleteByUserUid(user);

            String token = UUID.randomUUID().toString();

            String tokenHash = passwordEncoder.encode(token);

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .tokenHash(tokenHash)
                    .userUid(user)
                    .expiresAt(Instant.now().plusSeconds(900))
                    .build();

            passWordResetTokenRepository.save(passwordResetToken);

            emailServiceImpl.sendLinkResetPassword(email,token);
        });

        return ApiResponse.<Void>builder()
                .message("Vui lòng kiểm tra gmail để đổi mật khẩu !")
                .success(true)
                .build();
    }

    @Override
    public ApiResponse<Void> resetPassword(String rawToken, String newPassword) {
        List<PasswordResetToken> passwordResetTokens = passWordResetTokenRepository.findByExpiresAtAfter(Instant.now());

        PasswordResetToken passwordResetToken = passwordResetTokens.stream()
                .filter(passwordResetToken1 -> passwordEncoder.matches(rawToken,passwordResetToken1.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));

        if(passwordResetToken.isUsed()){
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
        }

        User user = userRepository.findByUid(passwordResetToken.getUserUid().getUid())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetToken.setUsedAt(Instant.now());
        passWordResetTokenRepository.save(passwordResetToken);

        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .success(true)
                .build();
    }



    // Private helpers — business logic
    private void verifyPassword(String rawPassword, User user) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            loginAttemptService.recordFailedAttempt(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void checkAccountStatus(User user) {
        if (!user.getAccountNonLocked()) {
            boolean lockExpired = user.getLockTime() != null && user.getLockTime().plusSeconds(LOCK_DURATION_SECONDS).isBefore(Instant.now());

            if (lockExpired) {
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            } else {
                throw new AppException(ErrorCode.USER_LOCKED);
            }
        }

        switch (user.getStatus()) {
            case PENDING -> throw new AppException(ErrorCode.PHONE_NOT_VERIFIED);
            case LOCKED, INACTIVE -> throw new AppException(ErrorCode.USER_INACTIVE);
            default -> {}
        }
    }

    private ApiResponse<UserLoginResponse> buildLoginResponse(User user, String deviceId, String deviceName) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user, deviceId, deviceName);

        user.setLastLogin(Instant.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        Wallet checkingWallet = user.getWallets().stream()
                .filter(acc -> acc.getWalletType() == WalletType.CHECKING)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        UserLoginResponse loginResponse = UserLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenDuration)
                .customerId(user.getCustomerId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .defaultAccountNumber(checkingWallet.getAccountNumber())
                .defaultAccountBalance(checkingWallet.getAvailableBalance())
                .build();

        return ApiResponse.<UserLoginResponse>builder()
                .message("Đăng nhập thành công")
                .success(true)
                .data(loginResponse)
                .build();
    }

    private void upsertDeviceSession(User user, String deviceId, String deviceName) {
        DeviceSession session = deviceSessionRepository.findByDeviceId(deviceId)
                .orElse(DeviceSession.builder()
                        .deviceId(deviceId)
                        .user(user)
                        .deviceName(deviceName)
                        .quickLoginEnabled(true)
                        .build());

        session.setLastLoginAt(Instant.now());
        deviceSessionRepository.save(session);
    }

    private User buildNewUser(CreateUserRequest request, Province province) {
        return User.builder()
                .customerId(customerIdGenerator.generateCustomerId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .province(province)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .accountNonLocked(true)
                .build();
    }


    // Private helpers — JWT
    private String generateAccessToken(User user) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getUid()))
                .claim("customerId", user.getCustomerId())
                .claim("role", user.getRole().toString())
                .claim("status", user.getStatus().toString())
                .claim("type", "access")
                .issuer("Digital Wallet")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(accessTokenDuration)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        return buildAndSignToken(claims);
    }

    private String generateRefreshToken(User user, String deviceId, String deviceName) {
        UUID jti = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenDuration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getUid()))
                .claim("type", "refresh")
                .claim("token_version", user.getTokenVersion())
                .issuer("Digital Wallet")
                .issueTime(new Date())
                .expirationTime(Date.from(expiresAt))
                .jwtID(String.valueOf(jti))
                .build();

        refreshTokenRepository.save(RefreshToken.builder()
                .jti(jti)
                .user(user)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .expiresAt(expiresAt)
                .build());

        return buildAndSignToken(claims);
    }

    private String buildAndSignToken(JWTClaimsSet claimsSet) {
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private SignedJWT verifyToken(String token , String expectedType) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        var verified = signedJWT.verify(jwsVerifier);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!verified || expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!expectedType.equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
}
