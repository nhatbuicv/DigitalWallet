package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.entity.User;

public interface LoginAttemptService {
    void recordFailedAttempt(User user);
}