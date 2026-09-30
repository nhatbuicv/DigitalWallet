package com.nhatbui.digital_wallet.service.auth;

import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.service.auth.impl.RateLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimitServiceImpl rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitServiceImpl(redisTemplate);
    }

    private void stubValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void check_firstRequest_shouldSetExpiryAndNotThrow() {
        stubValueOperations();
        when(valueOperations.increment("rate:otp:resend:a@b.c")).thenReturn(1L);

        rateLimitService.checkOtpResend("a@b.c");

        verify(redisTemplate).expire(eq("rate:otp:resend:a@b.c"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void checkOtpResend_secondRequestWithin60s_shouldThrow429() {
        stubValueOperations();
        when(valueOperations.increment("rate:otp:resend:a@b.c")).thenReturn(2L);

        assertThatThrownBy(() -> rateLimitService.checkOtpResend("a@b.c"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    ErrorCode code = ((AppException) e).getErrorCode();
                    assertThat(code).isEqualTo(ErrorCode.OTP_RESEND_COOLDOWN);
                    assertThat(code.getStatusCode().value()).isEqualTo(429);
                });

        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void checkOtpAttempt_overLimit_shouldThrow429() {
        stubValueOperations();
        when(valueOperations.increment("rate:otp:fail:a@b.c")).thenReturn(6L);

        assertThatThrownBy(() -> rateLimitService.checkOtpAttempt("a@b.c"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    ErrorCode code = ((AppException) e).getErrorCode();
                    assertThat(code).isEqualTo(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED);
                    assertThat(code.getStatusCode().value()).isEqualTo(429);
                });
    }

    @Test
    void isOtpLocked_shouldReflectCounter() {
        stubValueOperations();
        when(valueOperations.get("rate:otp:fail:a@b.c")).thenReturn("4", "5", null);

        assertThat(rateLimitService.isOtpLocked("a@b.c")).isFalse();
        assertThat(rateLimitService.isOtpLocked("a@b.c")).isTrue();
        assertThat(rateLimitService.isOtpLocked("a@b.c")).isFalse();
    }

    @Test
    void clearOtpAttempts_shouldDeleteCounter() {
        rateLimitService.clearOtpAttempts("a@b.c");

        verify(redisTemplate).delete("rate:otp:fail:a@b.c");
    }
}
