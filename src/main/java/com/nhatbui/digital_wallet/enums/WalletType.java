package com.nhatbui.digital_wallet.enums;

import lombok.Getter;

@Getter
public enum WalletType {
    CHECKING(1, "Tài khoản thanh toán"),
    SAVINGS(2, "Tài khoản tiết kiệm"),
    BUSINESS(3, "Tài khoản doanh nghiệp"),
    FOREIGN(4, "Tài khoản ngoại tệ"),
    PAYROLL(5, "Tài khoản trả lương"),
    INVESTMENT(6, "Tài khoản đầu tư"),
    JOINT(7, "Tài khoản đồng sở hữu");

    private final int code;
    private final String name;


    WalletType(int code , String name) {
        this.code = code;
        this.name = name;
    }

}
