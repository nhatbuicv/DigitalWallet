package com.nhatbui.digital_wallet.service.auth.impl;

import com.nhatbui.digital_wallet.service.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_TOKEN_KEY_PREFIX = "blacklist:token:";
    private static final long MIN_BLACKLIST_TTL_SECONDS = 1L;

    @Override
    public void blacklistToken(String tokenId, Instant expiresAt) {
        Duration remainingLifetime = Duration.between(Instant.now(), expiresAt);
        if (remainingLifetime.isNegative() || remainingLifetime.isZero()) {
            remainingLifetime = Duration.ofSeconds(MIN_BLACKLIST_TTL_SECONDS);
        }
        redisTemplate.opsForValue().set(blacklistedTokenKey(tokenId), "1", remainingLifetime);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistedTokenKey(tokenId)));
    }

    private static String blacklistedTokenKey(String tokenId) {
        return BLACKLIST_TOKEN_KEY_PREFIX + tokenId;
    }
}
