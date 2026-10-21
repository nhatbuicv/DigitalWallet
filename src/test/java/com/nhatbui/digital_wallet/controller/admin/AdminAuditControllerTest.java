package com.nhatbui.digital_wallet.controller.admin;

import com.nhatbui.digital_wallet.config.TestSecurityConfig;
import com.nhatbui.digital_wallet.controller.AdminAuditController;
import com.nhatbui.digital_wallet.dto.response.admin.AuditLogAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import com.nhatbui.digital_wallet.service.admin.AdminAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuditController.class)
@Import(TestSecurityConfig.class)
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuditService adminAuditService;

    private static final UUID TARGET_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_withAdminRole_shouldReturn200() throws Exception {
        AuditLogAdminResponse log = AuditLogAdminResponse.builder()
                .uid(UUID.randomUUID())
                .actorName("Admin")
                .action(AuditAction.WALLET_FREEZE)
                .targetType(AuditTargetType.WALLET)
                .targetUid(TARGET_UID)
                .targetIdentifier("123456789012")
                .createdAt(Instant.now())
                .build();

        PageResponse<AuditLogAdminResponse> pageResponse = PageResponse.<AuditLogAdminResponse>builder()
                .currentPage(0)
                .totalPage(1)
                .pageSize(20)
                .totalElements(1)
                .data(java.util.List.of(log))
                .build();

        when(adminAuditService.getAuditLogs(any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.data[0].action").value("WALLET_FREEZE"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getAuditLogs_withStaffRole_shouldReturn200() throws Exception {
        AuditLogAdminResponse log = AuditLogAdminResponse.builder()
                .uid(UUID.randomUUID())
                .actorName("Staff")
                .action(AuditAction.WALLET_FREEZE)
                .targetType(AuditTargetType.WALLET)
                .targetUid(TARGET_UID)
                .targetIdentifier("123456789012")
                .createdAt(Instant.now())
                .build();

        PageResponse<AuditLogAdminResponse> pageResponse = PageResponse.<AuditLogAdminResponse>builder()
                .currentPage(0)
                .totalPage(1)
                .pageSize(20)
                .totalElements(1)
                .data(java.util.List.of(log))
                .build();

        when(adminAuditService.getAuditLogs(any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAuditLogs_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuditLogs_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
    }
}
