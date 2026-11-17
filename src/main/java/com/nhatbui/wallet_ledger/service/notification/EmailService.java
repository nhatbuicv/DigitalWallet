package com.nhatbui.wallet_ledger.service.notification;

public interface EmailService {
    void sendLinkResetPassword (String email , String token);

    void sendToEmail(String email);

    void sendOtpEmail(String email , String fullName , String otp);
}
