package com.nhatbui.wallet_ledger.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

class JwtSecurityHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityErrorWriter writer = new SecurityErrorWriter(objectMapper);

    @Test
    void entryPoint_shouldReturn401Code1001() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(writer);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("bad token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asInt()).isEqualTo(1001);
        assertThat(body.get("success").asBoolean()).isFalse();
    }

    @Test
    void accessDeniedHandler_shouldReturn403Code1002() throws Exception {
        JwtAccessDeniedHandler deniedHandler = new JwtAccessDeniedHandler(writer);
        MockHttpServletResponse response = new MockHttpServletResponse();

        deniedHandler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("forbidden"));

        assertThat(response.getStatus()).isEqualTo(403);
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asInt()).isEqualTo(1002);
        assertThat(body.get("success").asBoolean()).isFalse();
    }

    // ===== CORS preflight =====

    private MockHttpServletResponse preflight(String origin, List<String> allowed) throws Exception {
        SecurityConfig config = new SecurityConfig();
        CorsConfigurationSource source = config.corsConfigurationSource(allowed);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/admin/users");
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "Authorization,Content-Type");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CorsFilter(source).doFilter(request, response, new org.springframework.mock.web.MockFilterChain());
        return response;
    }

    @Test
    void cors_preflightFromAllowedOrigin_shouldReturn200WithCorsHeaders() throws Exception {
        MockHttpServletResponse response = preflight("http://localhost:5173", List.of("http://localhost:5173"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
        assertThat(response.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(response.getHeader("Access-Control-Allow-Methods"))
                .contains("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS");
        assertThat(response.getHeader("Access-Control-Allow-Headers"))
                .contains("Authorization", "Content-Type");
    }

    @Test
    void cors_preflightFromUnknownOrigin_shouldBeRejected() throws Exception {
        MockHttpServletResponse response = preflight("http://evil.example.com", List.of("http://localhost:5173"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void cors_emptyAllowedOrigins_shouldRejectEveryOrigin() throws Exception {
        MockHttpServletResponse response = preflight("http://localhost:5173", List.of(""));

        assertThat(response.getStatus()).isEqualTo(403);
    }
}