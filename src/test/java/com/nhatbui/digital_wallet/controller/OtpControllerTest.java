package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.config.SecurityConfig;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.service.auth.OtpService;
import com.nhatbui.digital_wallet.service.notification.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OtpController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.signerKey=test-dummy-signer-key-must-be-at-least-32-bytes-long-0123456789")
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtpService otpService;

    @MockBean
    private EmailService emailService;

    @Test
    void sendOtp_withinCooldown_shouldReturn429() throws Exception {
        doThrow(new AppException(ErrorCode.OTP_RESEND_COOLDOWN))
                .when(emailService).sendToEmail(anyString());

        mockMvc.perform(post("/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(1012));
    }

    @Test
    void verifyOtp_whenAttemptLimitExceeded_shouldReturn429() throws Exception {
        when(otpService.verifyOtp(anyString(), anyString()))
                .thenThrow(new AppException(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED));

        mockMvc.perform(post("/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"otp\":\"123456\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(1013));
    }

    @Test
    void verifyOtp_withValidCode_shouldReturn200() throws Exception {
        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
