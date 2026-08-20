package com.nhatbui.digital_wallet.service.auth;

public interface OtpService {
    String generateOtp(String email);

    boolean verifyOtp(String email, String otp);

    void activateUser(String email);
}
