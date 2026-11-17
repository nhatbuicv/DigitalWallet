package com.nhatbui.wallet_ledger.service.idempotency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhatbui.wallet_ledger.dto.response.common.ApiResponse;
import com.nhatbui.wallet_ledger.enums.IdempotencyOperation;
import com.nhatbui.wallet_ledger.exception.ErrorCode;

import java.util.UUID;

public interface IdempotencyService {

    ClaimResult claim(UUID userId, IdempotencyOperation operation, String idempotencyKey, String requestHash);
    <T> void complete(UUID recordId, int httpStatus, ApiResponse<T> response);
    void fail(UUID recordId, ErrorCode errorCode);
    void release(UUID recordId);
    <T> T readResponse(ClaimResult claim, TypeReference<T> type);
}
