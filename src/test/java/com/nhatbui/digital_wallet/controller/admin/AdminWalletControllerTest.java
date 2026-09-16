package com.nhatbui.digital_wallet.controller.admin;

import com.nhatbui.digital_wallet.config.TestSecurityConfig;
import com.nhatbui.digital_wallet.controller.AdminWalletController;
import com.nhatbui.digital_wallet.dto.request.wallet.WalletStatusRequest;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.service.admin.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminWalletController.class)
@Import(TestSecurityConfig.class)
class AdminWalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminWalletService adminWalletService;

    private static final UUID WALLET_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    @WithMockUser(roles = "ADMIN")
    void freezeWallet_withAdminRole_shouldReturn200() throws Exception {
        WalletAdminResponse walletResponse = WalletAdminResponse.builder()
                .uid(WALLET_UID.toString())
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status("FROZEN")
                .build();

        when(adminWalletService.freezeWallet(eq(WALLET_UID), any(WalletStatusRequest.class))).thenReturn(
                ApiResponse.<WalletAdminResponse>builder()
                        .message("Đóng băng ví thành công")
                        .success(true)
                        .data(walletResponse)
                        .build());

        mockMvc.perform(patch("/admin/wallets/{walletUid}/freeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Fraudulent activity detected\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FROZEN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void freezeWallet_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/wallets/{walletUid}/freeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Fraudulent activity detected\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void freezeWallet_withoutReason_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/admin/wallets/{walletUid}/freeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unfreezeWallet_withAdminRole_shouldReturn200() throws Exception {
        WalletAdminResponse walletResponse = WalletAdminResponse.builder()
                .uid(WALLET_UID.toString())
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status("ACTIVE")
                .build();

        when(adminWalletService.unfreezeWallet(eq(WALLET_UID), any(WalletStatusRequest.class))).thenReturn(
                ApiResponse.<WalletAdminResponse>builder()
                        .message("Mở khóa ví thành công")
                        .success(true)
                        .data(walletResponse)
                        .build());

        mockMvc.perform(patch("/admin/wallets/{walletUid}/unfreeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Issue resolved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void unfreezeWallet_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/wallets/{walletUid}/unfreeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Issue resolved\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unfreezeWallet_withoutReason_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/admin/wallets/{walletUid}/unfreeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void freezeWallet_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(patch("/admin/wallets/{walletUid}/freeze", WALLET_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Fraudulent activity detected\"}"))
                .andExpect(status().isUnauthorized());
    }
}