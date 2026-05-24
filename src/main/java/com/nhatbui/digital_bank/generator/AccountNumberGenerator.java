package com.nhatbui.digital_bank.generator;

import com.nhatbui.digital_bank.enums.AccountType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountNumberGenerator {
    private static final int BANK_ID = 22;

    // Branch mặc định cho mở online
    private static final int DEFAULT_BRANCH = 99;

    // Tài khoản mặc định
    private static final AccountType DEFAULT_ACCOUNT_TYPE = AccountType.CHECKING;

    @PersistenceContext
    EntityManager entityManager;

    public String generate(Integer branchCode, AccountType accountType) {

        int actualBranch = branchCode == null ? DEFAULT_BRANCH : branchCode;

        AccountType actualType = accountType == null ? DEFAULT_ACCOUNT_TYPE : accountType;

        String bankPart   = String.format("%03d", BANK_ID);
        String branchPart = buildBranchPart(actualBranch, actualType);
        String serialPart = buildSerial();

        return bankPart + branchPart + serialPart;
    }

    private String buildBranchPart(int branchCode, AccountType accountType) {
        String branch = String.format("%02d", branchCode % 100);
        String type = String.valueOf(accountType.getCode());

        return branch + type;
    }

    private String buildSerial() {

        Long serial = ((Number) entityManager
                .createNativeQuery(
                        "SELECT nextval('account_serial_seq')")
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