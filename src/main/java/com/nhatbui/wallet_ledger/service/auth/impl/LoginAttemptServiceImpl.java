package com.nhatbui.wallet_ledger.service.auth.impl;

import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import com.nhatbui.wallet_ledger.service.auth.LoginAttemptService;
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
            user.setWalletNonLocked(false);
            user.setLockTime(Instant.now());
        }

        userRepository.save(user);
    }
}
