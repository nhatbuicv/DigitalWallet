package com.nhatbui.digital_bank.enums;

public enum AccountStatus {
    ACTIVE,      // Đang hoạt động
    INACTIVE,    // Không hoạt động
    FROZEN,      // Đông băng (tạm khóa)
    CLOSED,      // Đã đóng
    PENDING,     // Chờ kích hoạt
    SUSPENDED    // Bị treo
}
