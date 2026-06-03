package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.entity.User;

public interface LoginAttemptService {
    void recordFailedAttempt(User user);
}