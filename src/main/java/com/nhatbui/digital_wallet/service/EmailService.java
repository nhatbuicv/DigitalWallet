package com.nhatbui.digital_wallet.service;

public interface EmailService {
    void sendLinkResetPassword (String email , String token);

    void sendToEmail(String email);

    void sendOtpEmail(String email , String fullName , String otp);
}
