package com.nhatbui.digital_bank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // SYSTEM / COMMON ERRORS (9000 - 9999)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized system error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9001, "Uncategorized error key defined", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(9002, "Invalid request body or missing fields", HttpStatus.BAD_REQUEST),

    // SECURITY & AUTH MODULE (1001 - 1019)
    UNAUTHENTICATED(1001, "Unauthenticated. Please log in again", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1003, "Invalid phone number or password", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1004, "Session has expired. Please refresh your token", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1005, "Invalid token provider", HttpStatus.UNAUTHORIZED),

    // USER MODULE - REGISTRATION & ACCOUNT STATUS (1020 - 1049)
    REGISTRATION_FAILED(1020, "Registration failed. Please check your information or contact support", HttpStatus.CONFLICT),
    PASSWORD_NOT_MATCH(1021, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_EXISTS(1022, "Phone number is already registered", HttpStatus.BAD_REQUEST),
    CITIZEN_ID_ALREADY_EXISTS(1023, "Citizen ID is already registered", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1024, "User profile not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND(1025, "Account not found", HttpStatus.NOT_FOUND),


    // ACCOUNT BANKING STATUS (1050 - 1099)
    ACCOUNT_INACTIVE(1050, "Your main user account is inactive", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1051, "Your account has been locked due to multiple failed login attempts", HttpStatus.LOCKED), // Dùng HttpStatus.LOCKED (423) chuẩn hơn
    PHONE_NOT_VERIFIED(1052, "Your phone number has not been verified via OTP", HttpStatus.FORBIDDEN),

    // LOCATION / REGION MODULE
    PROVINCE_NOT_FOUND(1101, "Province not found", HttpStatus.BAD_REQUEST)
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
