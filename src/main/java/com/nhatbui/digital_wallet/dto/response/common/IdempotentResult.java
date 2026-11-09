package com.nhatbui.digital_wallet.dto.response.common;

public record IdempotentResult<T>(T body, boolean replayed) {
    public static <T> IdempotentResult<T> fresh(T body) {
        return new IdempotentResult<>(body, false);
    }

    public static <T> IdempotentResult<T> replay(T body) {
        return new IdempotentResult<>(body, true);
    }
}
