package com.nhatbui.digital_wallet.config;

import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.auth.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionRevocationValidatorTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Jwt currentAccessToken;

    private SessionRevocationValidator sessionRevocationValidator;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        sessionRevocationValidator = new SessionRevocationValidator(tokenBlacklistService, userRepository);
        currentUserId = UUID.randomUUID();
    }

    @Test
    void validate_blacklistedAccessToken_shouldFail() {
        when(currentAccessToken.getId()).thenReturn("revoked-token-id");
        when(tokenBlacklistService.isTokenBlacklisted("revoked-token-id")).thenReturn(true);

        assertThat(sessionRevocationValidator.validate(currentAccessToken).hasErrors()).isTrue();
    }

    @Test
    void validate_accessTokenWithStaleVersion_shouldFail() {
        when(currentAccessToken.getId()).thenReturn("stale-token-id");
        when(tokenBlacklistService.isTokenBlacklisted("stale-token-id")).thenReturn(false);
        when(currentAccessToken.getClaimAsString("type")).thenReturn("access");
        when(currentAccessToken.getClaim("token_version")).thenReturn(1);
        when(currentAccessToken.getSubject()).thenReturn(currentUserId.toString());
        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(User.builder().tokenVersion(2).build()));

        assertThat(sessionRevocationValidator.validate(currentAccessToken).hasErrors()).isTrue();
    }

    @Test
    void validate_accessTokenWithFreshVersion_shouldPass() {
        when(currentAccessToken.getId()).thenReturn("fresh-token-id");
        when(tokenBlacklistService.isTokenBlacklisted("fresh-token-id")).thenReturn(false);
        when(currentAccessToken.getClaimAsString("type")).thenReturn("access");
        when(currentAccessToken.getClaim("token_version")).thenReturn(2);
        when(currentAccessToken.getSubject()).thenReturn(currentUserId.toString());
        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(User.builder().tokenVersion(2).build()));

        assertThat(sessionRevocationValidator.validate(currentAccessToken).hasErrors()).isFalse();
    }

    @Test
    void validate_accessTokenWithoutVersion_shouldFail() {
        when(currentAccessToken.getId()).thenReturn("legacy-token-id");
        when(tokenBlacklistService.isTokenBlacklisted("legacy-token-id")).thenReturn(false);
        when(currentAccessToken.getClaimAsString("type")).thenReturn("access");
        when(currentAccessToken.getClaim("token_version")).thenReturn(null);

        assertThat(sessionRevocationValidator.validate(currentAccessToken).hasErrors()).isTrue();
    }
}
