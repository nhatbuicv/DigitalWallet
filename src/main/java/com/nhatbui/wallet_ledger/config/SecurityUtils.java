package com.nhatbui.wallet_ledger.config;

import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
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

    public String getCurrentTokenId() {
        Jwt currentAccessToken = getCurrentJwt();
        return currentAccessToken == null ? null : currentAccessToken.getId();
    }

    public Instant getCurrentTokenExpiresAt() {
        Jwt currentAccessToken = getCurrentJwt();
        return currentAccessToken == null ? null : currentAccessToken.getExpiresAt();
    }

    private Jwt getCurrentJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }
        return null;
    }
}
