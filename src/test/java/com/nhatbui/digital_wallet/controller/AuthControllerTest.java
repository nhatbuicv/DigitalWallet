package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.config.JwtAccessDeniedHandler;
import com.nhatbui.digital_wallet.config.JwtAuthenticationEntryPoint;
import com.nhatbui.digital_wallet.config.SecurityConfig;
import com.nhatbui.digital_wallet.config.SecurityErrorWriter;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class, SecurityErrorWriter.class})
@TestPropertySource(properties = "jwt.signerKey=test-dummy-signer-key-must-be-at-least-32-bytes-long-0123456789")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void publicEndpoints_shouldContainForgotAndResetPassword() {
        assertThat(Arrays.asList(SecurityConfig.PUBLIC_ENDPOINT))
                .contains("/auth/forgot-password", "/auth/reset-password");
    }

    @Test
    void forgotPassword_withoutAuth_shouldReturn202() throws Exception {
        when(authService.requestPasswordReset(anyString(), anyString()))
                .thenReturn(ApiResponse.<Void>builder()
                        .code(1000)
                        .success(true)
                        .message("Password reset email sent")
                        .build());

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_withoutAuth_shouldReturn200() throws Exception {
        when(authService.resetPassword(anyString(), anyString()))
                .thenReturn(ApiResponse.<Void>builder()
                        .code(1000)
                        .success(true)
                        .message("Password reset successfully")
                        .build());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"raw-token\",\"newPassword\":\"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void logout_withoutAuth_shouldStillBeBlocked() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"dummy-refresh-token\"}"))
                .andExpect(status().isUnauthorized());
    }
}
