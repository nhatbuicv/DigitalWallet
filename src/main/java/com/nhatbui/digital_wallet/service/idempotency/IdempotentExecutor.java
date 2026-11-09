package com.nhatbui.digital_wallet.service.idempotency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class IdempotentExecutor {

    private final IdempotencyService idempotencyService;
    private final TransactionTemplate transactionTemplate;

    public <T> IdempotentResult<ApiResponse<T>> execute(UUID userId, IdempotencyOperation operation, String idempotencyKey,
                                                        String requestHash, TypeReference<ApiResponse<T>> responseType, Supplier<ApiResponse<T>> action) {
        ClaimResult claim = idempotencyService.claim(userId, operation, idempotencyKey, requestHash);

        return switch (claim.status()) {
            case REPLAY -> handleReplay(claim, responseType);
            case CONFLICT -> throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            case IN_PROGRESS -> throw new AppException(ErrorCode.IDEMPOTENCY_IN_PROGRESS);
            case OWNED -> executeOwned(claim.recordId(), action);
        };
    }

    private <T> IdempotentResult<ApiResponse<T>> handleReplay(ClaimResult claim, TypeReference<ApiResponse<T>> responseType) {
        ApiResponse<T> replayBody = idempotencyService.readResponse(claim, responseType);
        if (claim.httpStatus() != null && claim.httpStatus() >= 400 || !replayBody.isSuccess()) {
            throw new AppException(ErrorCode.fromCode(replayBody.getCode()), true);
        }
        return IdempotentResult.replay(replayBody);
    }

    private <T> IdempotentResult<ApiResponse<T>> executeOwned(UUID recordId, Supplier<ApiResponse<T>> action) {
        try {
            ApiResponse<T> response = transactionTemplate.execute(status -> {
                ApiResponse<T> result = action.get();
                idempotencyService.complete(recordId, 200, result);
                return result;
            });
            return IdempotentResult.fresh(response);
        } catch (AppException e) {
            if (e.getErrorCode().getStatusCode().is5xxServerError()) {
                idempotencyService.release(recordId);
            } else {
                idempotencyService.fail(recordId, e.getErrorCode());
            }
            throw e;
        } catch (Throwable t) {
            idempotencyService.release(recordId);
            throw t;
        }
    }
}
