package com.nhatbui.digital_wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhatbui.digital_wallet.entity.PasswordResetToken;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.repository.PasswordResetTokenRepository;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Task 04 (B3): old tokens must be rejected with 401 after logout,
 * logout-all, password change and password reset — verified against
 * REAL Postgres + Redis (Testcontainers).
 */
@AutoConfigureMockMvc
class SessionRevocationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String staffEmail;
    private String staffPassword;
    private String staffPhone;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        staffEmail = "staff-" + uniqueSuffix + "@example.com";
        staffPassword = "Password123";
        staffPhone = "09" + String.format("%08d", Math.abs(uniqueSuffix.hashCode()) % 100000000);

        userRepository.save(User.builder()
                .customerId("STAFF-" + uniqueSuffix)
                .fullName("Revoke Staff")
                .email(staffEmail)
                .phone(staffPhone)
                .password(passwordEncoder.encode(staffPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .phoneVerified(true)
                .failedLoginAttempts(0)
                .walletNonLocked(true)
                .tokenVersion(0)
                .build());
    }

    @Test
    void logout_shouldRevokeAccessAndRefresh_401() throws Exception {
        SessionTokens sessionTokens = login("device-logout-1");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + sessionTokens.refreshToken() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + sessionTokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());

        String blacklistedKey = "blacklist:token:" + tokenIdOf(sessionTokens.accessToken());
        assertThat(redisTemplate.hasKey(blacklistedKey)).isTrue();
        Long remainingTtlSeconds = redisTemplate.getExpire(blacklistedKey);
        assertThat(remainingTtlSeconds).isNotNull().isPositive().isLessThanOrEqualTo(3600);
    }

    @Test
    void logoutAll_shouldRevokeEveryDevice_401() throws Exception {
        SessionTokens deviceATokens = login("device-all-A");
        SessionTokens deviceBTokens = login("device-all-B");

        mockMvc.perform(post("/auth/logout-all")
                        .header("Authorization", "Bearer " + deviceATokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + deviceATokens.refreshToken() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + deviceATokens.accessToken()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + deviceBTokens.accessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + deviceATokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + deviceBTokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldRevokeOldSessions_401() throws Exception {
        SessionTokens sessionTokens = login("device-change-1");

        mockMvc.perform(put("/users/me/password")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"" + staffPassword + "\","
                                + "\"password\":\"NewPassword456\","
                                + "\"confirmPassword\":\"NewPassword456\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + sessionTokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());

        SessionTokens freshTokens = loginWithPassword("NewPassword456", "device-change-2");
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + freshTokens.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_shouldRevokeOldSessions_401() throws Exception {
        SessionTokens sessionTokens = login("device-reset-1");

        String rawResetToken = "raw-reset-token-" + UUID.randomUUID();
        User tokenOwner = userRepository.findByEmail(staffEmail).orElseThrow();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .tokenHash(passwordEncoder.encode(rawResetToken))
                .userUid(tokenOwner)
                .expiresAt(Instant.now().plusSeconds(900))
                .build());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + rawResetToken + "\","
                                + "\"newPassword\":\"ResetPassword789\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + sessionTokens.accessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + sessionTokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());

        SessionTokens freshTokens = loginWithPassword("ResetPassword789", "device-reset-2");
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + freshTokens.accessToken()))
                .andExpect(status().isOk());
    }

    private SessionTokens login(String deviceId) throws Exception {
        return loginWithPassword(staffPassword, deviceId);
    }

    private SessionTokens loginWithPassword(String rawPassword, String deviceId) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + staffEmail + "\","
                                + "\"clientType\":\"WEB_ADMIN\","
                                + "\"password\":\"" + rawPassword + "\","
                                + "\"deviceId\":\"" + deviceId + "\","
                                + "\"deviceName\":\"integration-test\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return new SessionTokens(
                loginBody.path("data").path("accessToken").asText(),
                loginBody.path("data").path("refreshToken").asText());
    }

    private static String tokenIdOf(String accessToken) throws Exception {
        return SignedJWT.parse(accessToken).getJWTClaimsSet().getJWTID();
    }

    private record SessionTokens(String accessToken, String refreshToken) {
    }
}
