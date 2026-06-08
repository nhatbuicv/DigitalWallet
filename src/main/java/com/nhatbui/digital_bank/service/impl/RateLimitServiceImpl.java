package com.nhatbui.digital_bank.service.impl;

import com.nhatbui.digital_bank.exception.AppException;
import com.nhatbui.digital_bank.exception.ErrorCode;
import com.nhatbui.digital_bank.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {
    private final StringRedisTemplate redisTemplate;

    private static final int RESET_MAX_REQUESTS = 3;
    private static final long RESET_WINDOW_SECONDS = 3600L;

    @Override
    public void checkResetLimit(String ipAddress) {
        String key = "rate:pwd_reset:" + ipAddress;
        check(key, RESET_MAX_REQUESTS, RESET_WINDOW_SECONDS, ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    @Override
    public void check(String key, int maxRequests, long windowSeconds, ErrorCode errorCode) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (count > maxRequests) {
            throw new AppException(errorCode);
        }
    }
}

