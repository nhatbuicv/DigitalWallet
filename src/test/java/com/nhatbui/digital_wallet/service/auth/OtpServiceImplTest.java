package com.nhatbui.digital_wallet.service.auth;

import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.auth.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private UserRepository userRepository;

    private OtpServiceImpl otpService;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(redisTemplate, rateLimitService, userRepository);
    }

    private void stubValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateOtp_shouldStoreCodeWith5MinuteTtl() {
        stubValueOperations();
        String otp = otpService.generateOtp(EMAIL);

        assertThat(otp).matches("^[0-9]{6}$");
        verify(rateLimitService).checkOtpResend(EMAIL);
        verify(valueOperations).set(eq("OTP:" + EMAIL), eq(otp), eq(Duration.ofMinutes(5)));
    }

    @Test
    void generateOtp_withinCooldown_shouldThrow429() {
        doThrow(new AppException(ErrorCode.OTP_RESEND_COOLDOWN))
                .when(rateLimitService).checkOtpResend(EMAIL);

        assertThatThrownBy(() -> otpService.generateOtp(EMAIL))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.OTP_RESEND_COOLDOWN));

        verify(valueOperations, never()).set(any(), any(), any());
    }

    @Test
    void verifyOtp_withCorrectCode_shouldReturnTrueAndClearAttempts() {
        stubValueOperations();
        when(rateLimitService.isOtpLocked(EMAIL)).thenReturn(false);
        when(valueOperations.get("OTP:" + EMAIL)).thenReturn("123456");

        assertThat(otpService.verifyOtp(EMAIL, "123456")).isTrue();

        verify(redisTemplate).delete("OTP:" + EMAIL);
        verify(rateLimitService).clearOtpAttempts(EMAIL);
        verify(rateLimitService, never()).checkOtpAttempt(any());
    }

    @Test
    void verifyOtp_withWrongCode_shouldReturnFalseAndCountAttempt() {
        stubValueOperations();
        when(rateLimitService.isOtpLocked(EMAIL)).thenReturn(false);
        when(valueOperations.get("OTP:" + EMAIL)).thenReturn("123456");

        assertThat(otpService.verifyOtp(EMAIL, "000000")).isFalse();

        verify(rateLimitService).checkOtpAttempt(EMAIL);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyOtp_whenLocked_shouldThrow429WithoutTouchingRedis() {
        when(rateLimitService.isOtpLocked(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, "123456"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED));

        verify(valueOperations, never()).get(any());
        verify(rateLimitService, never()).checkOtpAttempt(any());
    }

    @Test
    void verifyOtp_withExpiredCode_shouldReturnFalseWithoutCountingAttempt() {
        stubValueOperations();
        when(rateLimitService.isOtpLocked(EMAIL)).thenReturn(false);
        when(valueOperations.get("OTP:" + EMAIL)).thenReturn(null);

        assertThat(otpService.verifyOtp(EMAIL, "123456")).isFalse();

        verify(rateLimitService, never()).checkOtpAttempt(any());
    }
}
