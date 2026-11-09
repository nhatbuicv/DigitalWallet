package com.nhatbui.digital_wallet.service.idempotency;

import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.StringJoiner;

@Component
public class RequestHasher {
    public String hash(IdempotencyOperation operation, Object... fields) {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(operation == null ? "" : operation.name());
        if (fields != null) {
            for (Object field : fields) {
                joiner.add(field == null ? "" : field.toString().trim());
            }
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(joiner.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
// hash dữ liệu từ payload để kiểm tra trùng lặp và tính toàn vẹn của request (Idempotency)