package com.nhatbui.digital_bank.enums;

public enum AccountType {
    CHECKING(1),    // Tài khoản thanh toán
    SAVINGS(2),     // Tài khoản tiết kiệm
    BUSINESS(3),    // Tài khoản doanh nghiệp
    FOREIGN(4),     // Tài khoản ngoại tệ
    PAYROLL(5),     // Tài khoản trả lương
    INVESTMENT(6),  // Tài khoản đầu tư
    JOINT(7);        // Tài khoản chung

    private final int code;


    AccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
