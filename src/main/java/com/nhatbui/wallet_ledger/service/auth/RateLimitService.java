package com.nhatbui.wallet_ledger.service.auth;

import com.nhatbui.wallet_ledger.exception.ErrorCode;

public interface RateLimitService {
    void checkResetLimit(String ipAddress);

    void check(String key, int maxRequests, long windowSeconds, ErrorCode errorCode);

    void checkOtpResend(String email);

    void checkOtpAttempt(String email);

    boolean isOtpLocked(String email);

    void clearOtpAttempts(String email);
}
