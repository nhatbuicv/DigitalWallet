package com.nhatbui.digital_wallet.generator;

import com.nhatbui.digital_wallet.enums.WalletType;
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
    private static final int BANK_ID = 22;

    // Branch mặc định cho mở online
    private static final int DEFAULT_BRANCH = 99;

    // Tài khoản mặc định
    private static final WalletType DEFAULT_WALLET_TYPE = WalletType.CHECKING;

    @PersistenceContext
    EntityManager entityManager;

    public String generate(Integer branchCode, WalletType walletType) {

        int actualBranch = branchCode == null ? DEFAULT_BRANCH : branchCode;

        WalletType actualType = walletType == null ? DEFAULT_WALLET_TYPE : walletType;

        String bankPart   = String.format("%03d", BANK_ID);
        String branchPart = buildBranchPart(actualBranch, actualType);
        String serialPart = buildSerial();

        return bankPart + branchPart + serialPart;
    }

    private String buildBranchPart(int branchCode, WalletType walletType) {
        String branch = String.format("%02d", branchCode % 100);
        String type = String.valueOf(walletType.getCode());

        return branch + type;
    }

    private String buildSerial() {

        Long serial = ((Number) entityManager
                .createNativeQuery(
                        "SELECT nextval('wallet_serial_seq')")
                .getSingleResult())
                .longValue();

        return String.format("%06d", serial);
    }

    public static String getBankCode() {
        return String.format("%03d", BANK_ID % 100);
    }

    public static String getBankBranch() {
        return String.format("%02d", DEFAULT_BRANCH % 100);
    }
}

//[ Bank ID: 3 số ][ Branch + Type: 3 số ][ Serial: 6 số ]
//       079              101                  000042