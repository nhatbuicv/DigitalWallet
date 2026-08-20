package com.nhatbui.digital_wallet.service.auth.impl;

import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.auth.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final long OTP_EXPIRE_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;

    @Override
    public String generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set("OTP:" + email, otp, Duration.ofMinutes(OTP_EXPIRE_MINUTES));

        return otp;
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String savedOtp = redisTemplate.opsForValue().get("OTP:" + email);

        if(savedOtp == null){
            return false;
        }

        boolean valid = savedOtp.equals(otp);
        if(valid){
            redisTemplate.delete("OTP:" + email);
        }

        return valid;
    }

    @Override
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPhoneVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setWalletNonLocked(true);

        userRepository.save(user);
    }
}
