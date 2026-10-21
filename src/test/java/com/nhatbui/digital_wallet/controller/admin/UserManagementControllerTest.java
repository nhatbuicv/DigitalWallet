package com.nhatbui.digital_wallet.controller.admin;

import com.nhatbui.digital_wallet.config.TestSecurityConfig;
import com.nhatbui.digital_wallet.controller.UserManagementController;
import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.service.admin.AdminUserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementController.class)
@Import(TestSecurityConfig.class)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    private static final UUID USER_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_withAdminRole_shouldReturn200() throws Exception {
        PageResponse<UserAdminResponse> pageResponse = PageResponse.<UserAdminResponse>builder()
                .currentPage(0)
                .totalPage(1)
                .pageSize(20)
                .totalElements(1)
                .data(java.util.List.of(UserAdminResponse.builder()
                        .uid(USER_UID.toString())
                        .fullName("Test User")
                        .email("test@example.com")
                        .phone("0912345678")
                        .status("ACTIVE")
                        .role("USER")
                        .build()))
                .build();

        when(adminUserService.getUsers(any(), any(UserSearchRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.data[0].fullName").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_withSearchParams_shouldReturnFilteredResults() throws Exception {
        UserAdminResponse user = UserAdminResponse.builder()
                .uid(USER_UID.toString())
                .fullName("Test User")
                .email("test@example.com")
                .phone("0912345678")
                .status("ACTIVE")
                .role("USER")
                .build();

        PageResponse<UserAdminResponse> pageResponse = PageResponse.<UserAdminResponse>builder()
                .currentPage(0)
                .totalPage(1)
                .pageSize(20)
                .totalElements(1)
                .data(java.util.List.of(user))
                .build();

        when(adminUserService.getUsers(any(), any(UserSearchRequest.class))).thenReturn(pageResponse);

        // Test search by phone
        mockMvc.perform(get("/admin/users")
                        .param("phone", "0912345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // Test search by email
        mockMvc.perform(get("/admin/users")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test search by fullName
        mockMvc.perform(get("/admin/users")
                        .param("fullName", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test filter by status
        mockMvc.perform(get("/admin/users")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test filter by role
        mockMvc.perform(get("/admin/users")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test search with multiple params
        mockMvc.perform(get("/admin/users")
                        .param("phone", "0912345678")
                        .param("status", "ACTIVE")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUsers_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getUsers_withStaffRole_shouldReturn200() throws Exception {
        PageResponse<UserAdminResponse> pageResponse = PageResponse.<UserAdminResponse>builder()
                .currentPage(0)
                .totalPage(1)
                .pageSize(20)
                .totalElements(1)
                .data(java.util.List.of(UserAdminResponse.builder()
                        .uid(USER_UID.toString())
                        .fullName("Test User")
                        .email("test@example.com")
                        .phone("0912345678")
                        .status("ACTIVE")
                        .role("USER")
                        .build()))
                .build();

        when(adminUserService.getUsers(any(), any(UserSearchRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserDetail_withAdminRole_shouldReturn200() throws Exception {
        UserAdminResponse response = UserAdminResponse.builder()
                .uid(USER_UID.toString())
                .fullName("Test User")
                .email("test@example.com")
                .phone("0912345678")
                .status("ACTIVE")
                .role("USER")
                .totalWallets(1)
                .totalBalance(100000L)
                .build();

        when(adminUserService.getUserDetail(USER_UID)).thenReturn(
                ApiResponse.<UserAdminResponse>builder()
                        .message("Lấy thông tin người dùng thành công")
                        .success(true)
                        .data(response)
                        .build());

        mockMvc.perform(get("/admin/users/{uid}", USER_UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.totalWallets").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserDetail_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/users/{uid}", USER_UID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getUserDetail_withStaffRole_shouldReturn200() throws Exception {
        UserAdminResponse response = UserAdminResponse.builder()
                .uid(USER_UID.toString())
                .fullName("Test User")
                .email("test@example.com")
                .phone("0912345678")
                .status("ACTIVE")
                .role("USER")
                .build();

        when(adminUserService.getUserDetail(USER_UID)).thenReturn(
                ApiResponse.<UserAdminResponse>builder()
                        .message("Lấy thông tin người dùng thành công")
                        .success(true)
                        .data(response)
                        .build());

        mockMvc.perform(get("/admin/users/{uid}", USER_UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uid").value(USER_UID.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserStatus_withAdminRole_shouldReturn200() throws Exception {
        when(adminUserService.changeUserStatus(eq(USER_UID), eq(UserStatus.LOCKED))).thenReturn(
                ApiResponse.<Void>builder()
                        .message("Cập nhật trạng thái người dùng thành công")
                        .success(true)
                        .build());

        mockMvc.perform(patch("/admin/users/{uid}/status", USER_UID)
                        .param("status", "LOCKED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void changeUserStatus_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/users/{uid}/status", USER_UID)
                        .param("status", "LOCKED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void changeUserStatus_withStaffRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/users/{uid}/status", USER_UID)
                        .param("status", "LOCKED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_withAdminRole_shouldReturn200() throws Exception {
        when(adminUserService.changeUserRole(eq(USER_UID), eq(UserRole.MANAGER))).thenReturn(
                ApiResponse.<Void>builder()
                        .message("Cập nhật vai trò người dùng thành công")
                        .success(true)
                        .build());

        mockMvc.perform(patch("/admin/users/{uid}/role", USER_UID)
                        .param("role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void changeUserRole_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/users/{uid}/role", USER_UID)
                        .param("role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void changeUserRole_withStaffRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/admin/users/{uid}/role", USER_UID)
                        .param("role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}