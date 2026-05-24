package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.RegisterResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_bank.entity.Account;
import com.nhatbui.digital_bank.entity.Province;
import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.enums.UserRole;
import com.nhatbui.digital_bank.enums.UserStatus;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.generator.CustomerIdGenerator;
import com.nhatbui.digital_bank.mapper.AccountMapper;
import com.nhatbui.digital_bank.mapper.UserMapper;
import com.nhatbui.digital_bank.repository.AccountRepository;
import com.nhatbui.digital_bank.repository.ProvinceRepository;
import com.nhatbui.digital_bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
                .emailVerified(false)
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
                .message("Đăng ký thành công! Vui lòng xác thực email")
                .data(registerResponse)
                .nextStep("VERIFY_EMAIL")
                .build();
    }
}
