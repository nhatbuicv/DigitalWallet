package com.nhatbui.digital_wallet.service.idempotency.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.entity.IdempotencyRecord;
import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.enums.IdempotencyStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.IdempotencyRecordRepository;
import com.nhatbui.digital_wallet.service.idempotency.ClaimResult;
import com.nhatbui.digital_wallet.service.idempotency.ClaimResult.ClaimStatus;
import com.nhatbui.digital_wallet.service.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${idempotency.ttl:24h}")
    private Duration ttl;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClaimResult claim(UUID userId, IdempotencyOperation operation, String idempotencyKey, String requestHash) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        int inserted = repository.tryInsert(
                id,
                userId,
                operation.name(),
                idempotencyKey,
                requestHash,
                now,
                now.plus(ttl));

        if (inserted == 1) {
            return new ClaimResult(id, ClaimStatus.OWNED, null, null);
        }

        IdempotencyRecord existing = repository
                .findByUserIdAndOperationAndIdemKey(userId, operation, idempotencyKey)
                .orElseThrow(() -> new AppException(ErrorCode.CONFLICT));

        // Chỉ bản ghi đã có kết quả cuối cùng mới được thay thế khi hết TTL.
        // IN_PROGRESS / SUSPECTED_TIMEOUT tuyệt đối không bị xóa để chống trừ tiền 2 lần.
        if (existing.getExpiresAt() != null && existing.getExpiresAt().isBefore(now)
                && isFinal(existing.getStatus())) {
            repository.deleteById(existing.getId());
            repository.flush();
            UUID newId = UUID.randomUUID();
            int reinserted = repository.tryInsert(
                    newId,
                    userId,
                    operation.name(),
                    idempotencyKey,
                    requestHash,
                    now,
                    now.plus(ttl));
            if (reinserted == 1) {
                return new ClaimResult(newId, ClaimStatus.OWNED, null, null);
            }
            existing = repository
                    .findByUserIdAndOperationAndIdemKey(userId, operation, idempotencyKey)
                    .orElseThrow(() -> new AppException(ErrorCode.CONFLICT));
        }

        if (!existing.getRequestHash().trim().equals(requestHash)) {
            return new ClaimResult(existing.getId(), ClaimStatus.CONFLICT, null, null);
        }

        if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            return new ClaimResult(existing.getId(), ClaimStatus.IN_PROGRESS, null, null);
        }

        if (existing.getStatus() == IdempotencyStatus.SUSPECTED_TIMEOUT) {
            // Kết quả của request trước chưa rõ -> không bao giờ thực thi lại khi chưa đối soát
            throw new AppException(ErrorCode.TRANSACTION_STATUS_UNCERTAIN);
        }

        return new ClaimResult(
                existing.getId(),
                ClaimStatus.REPLAY,
                existing.getHttpStatus(),
                existing.getResponseBody());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public <T> void complete(UUID recordId, int httpStatus, ApiResponse<T> response) {
        String body = serialize(response);
        int updated = repository.markFinished(
                recordId,
                IdempotencyStatus.COMPLETED,
                httpStatus,
                body,
                Instant.now());
        if (updated == 0) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID recordId, ErrorCode errorCode) {
        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .success(false)
                .build();
        String body = serialize(response);
        repository.markFinished(
                recordId,
                IdempotencyStatus.FAILED,
                errorCode.getStatusCode().value(),
                body,
                Instant.now());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(UUID recordId) {
        repository.deleteById(recordId);
    }

    @Override
    public <T> T readResponse(ClaimResult claim, TypeReference<T> type) {
        try {
            return objectMapper.readValue(claim.responseBody(), type);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private static boolean isFinal(IdempotencyStatus status) {
        return status == IdempotencyStatus.COMPLETED || status == IdempotencyStatus.FAILED;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
