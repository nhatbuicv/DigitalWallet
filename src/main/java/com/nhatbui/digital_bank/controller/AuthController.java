package com.nhatbui.digital_bank.controller;

import com.nhatbui.digital_bank.dto.request.CreateUserRequest;
import com.nhatbui.digital_bank.dto.response.ApiResponse;
import com.nhatbui.digital_bank.dto.response.RegisterResponse;
import com.nhatbui.digital_bank.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API xác thực và đăng ký người dùng")
public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Receive register request for email: {}", request.getEmail());

        ApiResponse<RegisterResponse> response = authService.registerUserWithDefaultAccount(request);

        if (!response.isSuccess()) {
            log.warn("Register failed for email: {}, reason: {}",
                    request.getEmail(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        log.info("Register success for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}