package com.nhatbui.wallet_ledger.service.idempotency;

import java.util.UUID;

public record ClaimResult(UUID recordId, ClaimStatus status, Integer httpStatus, String responseBody) {
    public enum ClaimStatus {
        OWNED,
        REPLAY,
        CONFLICT,
        IN_PROGRESS
    }
}
