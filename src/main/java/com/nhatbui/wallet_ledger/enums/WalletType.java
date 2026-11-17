package com.nhatbui.wallet_ledger.enums;

import lombok.Getter;

@Getter
public enum WalletType {
    PERSONAL(1, "Ví cá nhân"),
    MERCHANT(2, "Ví doanh nghiệp / Cửa hàng"),
    SYSTEM(8, "Ví hệ thống");

    private final int code;
    private final String name;


    WalletType(int code , String name) {
        this.code = code;
        this.name = name;
    }

}
