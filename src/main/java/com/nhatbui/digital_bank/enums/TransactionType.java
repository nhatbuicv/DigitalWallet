package com.nhatbui.digital_bank.enums;

public enum TransactionType {
    DEPOSIT,    // Giao dịch nộp tiền vào tài khoản
    WITHDRAW,   // Giao dịch rút tiền mặt khỏi tài khoản
    TRANSFER,   // Giao dịch chuyển tiền nội bộ hoặc liên ngân hàng
    PAYMENT,    // Giao dịch thanh toán hóa đơn, dịch vụ, mua sắm
    REFUND      // Giao dịch hoàn tiền (khi hủy đơn hoặc tra soát thành công)
}