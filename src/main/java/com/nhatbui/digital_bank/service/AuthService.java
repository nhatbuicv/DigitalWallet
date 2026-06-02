package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.request.PasswordLoginRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.auth.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.auth.UserLoginResponse;
import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.Province;
import com.nhatbui.digital_bank.entity.RefreshToken;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.AccountType;
import com.nhatbui.digital_bank.enums.UserRole;
import com.nhatbui.digital_bank.enums.UserStatus;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.generator.CustomerIdGenerator;
import com.nhatbui.digital_bank.mapper.AccountMapper;
import com.nhatbui.digital_bank.mapper.UserMapper;
import com.nhatbui.digital_bank.repository.AccountRepository;
import com.nhatbui.digital_bank.repository.ProvinceRepository;
import com.nhatbui.digital_bank.repository.RefreshTokenRepository;
import com.nhatbui.digital_bank.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProvinceRepository provinceRepository;
    private final CustomerIdGenerator customerIdGenerator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.access-token-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refresh-token-duration}")
    private long REFRESHABLE_DURATION;

    @Transactional
    public ApiResponse<RegisterResponse> registerUserWithDefaultAccount(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.REGISTRATION_FAILED);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.REGISTRATION_FAILED);
        }

        String customerId = customerIdGenerator.generateCustomerId();

        Province province = provinceRepository.findByCode(request.getCityCode())
                .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));


        User user = User.builder()
                .customerId(customerId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .province(province)
                .dateOfBirth(LocalDate.parse(request.getDateOfBirth()))
                .address(request.getAddress())
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .phoneVerified(false)
                .failedLoginAttempts(0)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);

        Account defaultAccount = accountService.createDefaultAccount(savedUser);
        Account savedAccount = accountRepository.save(defaultAccount);

        UserProfileResponse userResponse = userMapper.mapToUserProfileResponse(savedUser);
        AccountResponse accountResponse = accountMapper.mapToAccountResponse(savedAccount);

        RegisterResponse registerResponse = RegisterResponse.builder()
                .user(userResponse)
                .account(accountResponse)
                .build();

        return ApiResponse.<RegisterResponse>builder()
                .success(true)
                .message("Đăng ký thành công")
                .data(registerResponse)
                .build();
    }


    @Transactional
    public ApiResponse<UserLoginResponse> loginWithPassword(PasswordLoginRequest request) {

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getAccountNonLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusSeconds(1800).isBefore(Instant.now())) {
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            } else {
                throw new AppException(ErrorCode.ACCOUNT_LOCKED);
            }
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(ErrorCode.PHONE_NOT_VERIFIED);
        }
        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }


        refreshTokenRepository.deleteAllByUserUid(user.getUid());

        String accessToken  = generateAccessToken(user);

        String refreshToken = generateRefreshToken(user, request.getDeviceId(), request.getDeviceName());

        Account defaultAccount = user.getAccounts().stream()
                .filter(acc -> acc.getAccountType() == AccountType.CHECKING)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        user.setLastLogin(Instant.now());
        user.setFailedLoginAttempts(0); // Reset bộ đếm sai về 0
        userRepository.save(user);

        return ApiResponse.<UserLoginResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(UserLoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(VALID_DURATION)
                        .customerId(user.getCustomerId())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .defaultAccountNumber(defaultAccount.getAccountNumber())
                        .defaultAccountBalance(defaultAccount.getAvailableBalance())
                        .build())
                .build();
    }

    private String buildAndSignToken(JWTClaimsSet claimsSet) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(claimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateAccessToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getUid()))
                .claim("customerId", user.getCustomerId())
                .claim("role", user.getRole().toString())
                .claim("status", user.getStatus().toString())
                .claim("type", "access")
                .issuer("Digital Bank")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(VALID_DURATION)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        return buildAndSignToken(jwtClaimsSet);
    }

    public String generateRefreshToken(User user, String deviceId, String deviceName) {
        String jti = UUID.randomUUID().toString();

        Instant expiryInstant = Instant.now().plusSeconds(REFRESHABLE_DURATION);

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getUid()))
                .claim("type", "refresh")
                .claim("token_version", user.getTokenVersion())
                .issuer("Digital Bank")
                .issueTime(new Date())
                .expirationTime(Date.from(expiryInstant))
                .jwtID(jti)
                .build();

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .expiresAt(expiryInstant)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return buildAndSignToken(jwtClaimsSet);
    }


}
