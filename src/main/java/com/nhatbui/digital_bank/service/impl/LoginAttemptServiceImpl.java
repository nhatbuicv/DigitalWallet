package com.nhatbui.digital_bank.service.impl;

import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.repository.UserRepository;
import com.nhatbui.digital_bank.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= 5) {
            user.setAccountNonLocked(false);
            user.setLockTime(Instant.now());
        }

        userRepository.save(user);
    }
}
