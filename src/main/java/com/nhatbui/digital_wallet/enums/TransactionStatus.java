package com.nhatbui.digital_wallet.enums;

public enum TransactionStatus {
    PENDING,    // Giao dịch mới tiếp nhận, chờ OTP hoặc kiểm tra điều kiện
    PROCESSING, // Đang kết nối Core Banking / NAPAS để xử lý chuyển tiền
    SUCCESS,    // Giao dịch thành công hoàn toàn, đã hạch toán dòng tiền
    FAILED,     // Giao dịch thất bại (sai PIN, thiếu số dư, lỗi hệ thống)
    REVERSED    // Giao dịch bị hoàn tác, đảo dòng tiền trả lại tài khoản gốc
}
