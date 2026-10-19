package com.nhatbui.digital_wallet.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void errorCodes_shouldBeUnique_noMoreDuplicate1050() {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .filter(code -> !seen.add(code))
                .collect(Collectors.toSet());
        assertThat(duplicates).isEmpty();
    }

    @Test
    void userStatusCodes_shouldBeDistinct() {
        assertThat(ErrorCode.USER_LOCKED.getCode()).isNotEqualTo(ErrorCode.USER_INACTIVE.getCode());
        assertThat(ErrorCode.USER_INACTIVE.getCode()).isNotEqualTo(ErrorCode.PHONE_NOT_VERIFIED.getCode());
    }

    @Test
    void handleAppException_shouldKeepDomainCodeAndStatus() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleAppException(new AppException(ErrorCode.USER_INACTIVE));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.USER_INACTIVE.getCode());
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleAccessDenied_shouldReturn403Code1002() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getCode()).isEqualTo(1002);
    }

    @Test
    void handleAuthentication_shouldReturn401Code1001() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleAuthenticationException(new BadCredentialsException("bad"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getCode()).isEqualTo(1001);
    }

    @Test
    void handleNotReadable_shouldReturn400_not500() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleNotReadable(new HttpMessageNotReadableException("bad json"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getCode()).isEqualTo(9002);
    }

    @Test
    void handleNoResource_shouldReturn404_not500() {
        ResponseEntity<ApiResponse<?>> response = handler.handleNoResource(
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/nope"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getCode()).isEqualTo(9004);
    }

    @Test
    void handleMethodNotSupported_shouldReturn405_not500() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("DELETE"));

        assertThat(response.getStatusCode().value()).isEqualTo(405);
        assertThat(response.getBody().getCode()).isEqualTo(9005);
    }

    @Test
    void handleOptimisticLock_shouldReturn409_not500() {
        ResponseEntity<ApiResponse<?>> response = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("version conflict"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getCode()).isEqualTo(9006);
    }

    @Test
    void fallback_shouldStillReturn500_forTrulyUnexpected() {
        ResponseEntity<ApiResponse<?>> response =
                handler.handleRuntimeException(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getCode()).isEqualTo(9999);
    }
}
