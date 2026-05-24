package com.nhatbui.digital_bank.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPublicResponse {

    private String uid;
    private String fullName;
    private String email;
    private String phone;

    // Masked citizen ID (ẩn số)
    private String citizenId;

    // Chỉ lấy ngày, không lấy giờ
    private String dateOfBirth;

    // Trạng thái (ẩn thông tin bảo mật)
    private String status;

}