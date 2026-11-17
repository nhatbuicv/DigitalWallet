package com.nhatbui.wallet_ledger.service.auth;

import com.nhatbui.wallet_ledger.entity.User;

public interface LoginAttemptService {
    void recordFailedAttempt(User user);
}