package com.nhatbui.digital_wallet.service;

public interface EmailService {
    void sendLinkResetPassword (String email , String token);
}
