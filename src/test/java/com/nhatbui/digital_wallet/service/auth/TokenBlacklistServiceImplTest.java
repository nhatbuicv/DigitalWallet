package com.nhatbui.digital_wallet.service.auth;

import com.nhatbui.digital_wallet.service.auth.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistServiceImpl(redisTemplate);
    }

    @Test
    void blacklistToken_shouldStoreWithTtlEqualToRemainingLifetime() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Instant tokenExpiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklistToken("revoked-token-id", tokenExpiresAt);

        verify(valueOperations).set(
                eq("blacklist:token:revoked-token-id"),
                eq("1"),
                argThat(remainingLifetime -> remainingLifetime != null
                        && !remainingLifetime.isNegative()
                        && remainingLifetime.compareTo(Duration.ofSeconds(3600)) <= 0));
    }

    @Test
    void isTokenBlacklisted_shouldReflectRedisKey() {
        when(redisTemplate.hasKey("blacklist:token:revoked-token-id")).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:token:fresh-token-id")).thenReturn(false);

        assertThat(tokenBlacklistService.isTokenBlacklisted("revoked-token-id")).isTrue();
        assertThat(tokenBlacklistService.isTokenBlacklisted("fresh-token-id")).isFalse();
    }
}
