package com.nhatbui.wallet_ledger.generator;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ReferenceCodeGenerator {
    public String generateReferenceCode() {
        return "TXN" + Instant.now().toEpochMilli()
                + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }
}
