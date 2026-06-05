package com.nhatbui.digital_bank.config;

import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityUtils {

    private final UserRepository userRepository;

    public UUID getCurrentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }

    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
