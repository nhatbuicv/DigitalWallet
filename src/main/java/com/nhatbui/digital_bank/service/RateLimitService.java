package com.nhatbui.digital_bank.service;

import com.nhatbui.digital_bank.exception.ErrorCode;

public interface RateLimitService {
    void checkResetLimit(String ipAddress);

    void check(String key, int maxRequests, long windowSeconds, ErrorCode errorCode);
}
