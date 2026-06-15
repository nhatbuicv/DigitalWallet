package com.nhatbui.digital_wallet.enums;

public enum WalletStatus {
    ACTIVE,      // Đang hoạt động
    INACTIVE,    // Không hoạt động
    FROZEN,      // Đông băng (tạm khóa)
    CLOSED,      // Đã đóng
    PENDING,     // Chờ kích hoạt
    SUSPENDED    // Bị treo
}
