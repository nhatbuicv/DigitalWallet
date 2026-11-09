package com.nhatbui.digital_wallet.service.idempotency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.exception.ErrorCode;

import java.util.UUID;

public interface IdempotencyService {

    ClaimResult claim(UUID userId, IdempotencyOperation operation, String idempotencyKey, String requestHash);
    <T> void complete(UUID recordId, int httpStatus, ApiResponse<T> response);
    void fail(UUID recordId, ErrorCode errorCode);
    void release(UUID recordId);
    <T> T readResponse(ClaimResult claim, TypeReference<T> type);
}
