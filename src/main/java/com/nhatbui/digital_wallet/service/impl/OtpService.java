package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final long OTP_EXPIRE_MINUTES = 5;
    private final UserRepository userRepository;

    public String generateOtp(String phone) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + phone, otp, Duration.ofMinutes(OTP_EXPIRE_MINUTES));

        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {
        String savedOtp = redisTemplate.opsForValue().get("OTP:" + phone);

        if(savedOtp == null){
            return false;
        }

        boolean valid = savedOtp.equals(otp);
        if(valid){
            redisTemplate.delete("OTP:" + phone);
        }

        return valid;
    }

    public void activateUser(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPhoneVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setWalletNonLocked(true);

        userRepository.save(user);
    }
}