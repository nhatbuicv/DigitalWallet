package com.nhatbui.digital_bank.service;

public interface EmailService {
    void sendLinkResetPassword (String email , String token);
}
