package com.nhatbui.digital_wallet.config;

import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionRevocationValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt currentAccessToken) {
        String revokedTokenId = currentAccessToken.getId();
        if (tokenBlacklistService.isTokenBlacklisted(revokedTokenId)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("token_revoked", "Token has been revoked", null));
        }

        String tokenType = currentAccessToken.getClaimAsString("type");
        if ("access".equals(tokenType) && !hasFreshTokenVersion(currentAccessToken)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("token_revoked", "Stale token version, please log in again", null));
        }
        return OAuth2TokenValidatorResult.success();
    }

    private boolean hasFreshTokenVersion(Jwt currentAccessToken) {
        Number tokenVersionNumber = currentAccessToken.getClaim("token_version");
        if (tokenVersionNumber == null) {
            return false;
        }
        UUID currentUserId;
        try {
            currentUserId = UUID.fromString(currentAccessToken.getSubject());
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return userRepository.findById(currentUserId)
                .map(currentUser -> tokenVersionNumber.intValue() == currentUser.getTokenVersion())
                .orElse(false);
    }
}
