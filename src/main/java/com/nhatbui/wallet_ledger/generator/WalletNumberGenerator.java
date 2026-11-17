package com.nhatbui.wallet_ledger.generator;

import com.nhatbui.wallet_ledger.enums.WalletType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletNumberGenerator {
    private static final String WALLET_PREFIX = "02200";

    private static final WalletType DEFAULT_WALLET_TYPE = WalletType.PERSONAL;

    @PersistenceContext
    EntityManager entityManager;

    public String generate(WalletType walletType) {
        WalletType actualType = walletType == null ? DEFAULT_WALLET_TYPE : walletType;
        String serialPart = buildSerial();

        return WALLET_PREFIX + actualType.getCode() + serialPart;
    }

    private String buildSerial() {

        Long serial = ((Number) entityManager
                .createNativeQuery(
                        "SELECT nextval('wallet_serial_seq')")
                .getSingleResult())
                .longValue();

        return String.format("%06d", serial);
    }
}