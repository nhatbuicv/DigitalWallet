package com.nhatbui.digital_wallet.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

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
}
