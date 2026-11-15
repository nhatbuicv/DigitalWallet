package com.nhatbui.digital_wallet.controller.admin;

import com.nhatbui.digital_wallet.config.TestSecurityConfig;
import com.nhatbui.digital_wallet.controller.AdminTransactionController;
import com.nhatbui.digital_wallet.service.transaction.AdminTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTransactionController.class)
@Import(TestSecurityConfig.class)
class AdminTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTransactionService adminTransactionService;

    @Test
    void getTransactions_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/admin/transactions"))
                .andExpect(status().isUnauthorized());
    }
    // ===== RBAC matrix: ADMIN / MANAGER / STAFF / USER =====

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asRole(String role) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("rbac-user").roles(role);
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0} -> {1}")
    @org.junit.jupiter.params.provider.CsvSource({"ADMIN,200", "MANAGER,200", "STAFF,200", "USER,403"})
    void rbac_getTransactions(String role, int expectedStatus) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/transactions").with(asRole(role)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(expectedStatus));
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0} -> {1}")
    @org.junit.jupiter.params.provider.CsvSource({"ADMIN,200", "MANAGER,200", "STAFF,200", "USER,403"})
    void rbac_getTransactionDetail(String role, int expectedStatus) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/transactions/123e4567-e89b-12d3-a456-426614174000").with(asRole(role)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(expectedStatus));
    }
}
