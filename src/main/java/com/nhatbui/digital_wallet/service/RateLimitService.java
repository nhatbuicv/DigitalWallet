package com.nhatbui.digital_wallet.service;

import com.nhatbui.digital_wallet.exception.ErrorCode;

public interface RateLimitService {
    void checkResetLimit(String ipAddress);

    void check(String key, int maxRequests, long windowSeconds, ErrorCode errorCode);
}
