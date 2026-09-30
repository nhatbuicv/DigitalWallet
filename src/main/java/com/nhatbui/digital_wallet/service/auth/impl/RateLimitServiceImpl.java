package com.nhatbui.digital_wallet.service.auth.impl;

import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.service.auth.RateLimitService;
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

    private static final int OTP_RESEND_MAX_REQUESTS = 1;
    private static final long OTP_RESEND_WINDOW_SECONDS = 60L;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final long OTP_ATTEMPT_WINDOW_SECONDS = 600L;

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

    @Override
    public void checkOtpResend(String email) {
        check("rate:otp:resend:" + email, OTP_RESEND_MAX_REQUESTS, OTP_RESEND_WINDOW_SECONDS,
                ErrorCode.OTP_RESEND_COOLDOWN);
    }

    @Override
    public void checkOtpAttempt(String email) {
        check(otpFailKey(email), OTP_MAX_ATTEMPTS, OTP_ATTEMPT_WINDOW_SECONDS,
                ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED);
    }

    @Override
    public boolean isOtpLocked(String email) {
        String value = redisTemplate.opsForValue().get(otpFailKey(email));
        if (value == null) {
            return false;
        }
        try {
            return Integer.parseInt(value) >= OTP_MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void clearOtpAttempts(String email) {
        redisTemplate.delete(otpFailKey(email));
    }

    private static String otpFailKey(String email) {
        return "rate:otp:fail:" + email;
    }
}

