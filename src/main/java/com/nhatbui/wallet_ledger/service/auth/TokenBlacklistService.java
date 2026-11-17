package com.nhatbui.wallet_ledger.service.auth;

import java.time.Instant;

public interface TokenBlacklistService {
    void blacklistToken(String tokenId, Instant expiresAt);

    boolean isTokenBlacklisted(String tokenId);
}
