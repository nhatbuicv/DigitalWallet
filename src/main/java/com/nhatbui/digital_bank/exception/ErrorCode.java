package com.nhatbui.digital_bank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // SYSTEM / COMMON ERRORS (9000 - 9999)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized system error", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR(9000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9001, "Uncategorized error key defined", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(9002, "Invalid request body or missing fields", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(9003, "Too many requests. Please try again later", HttpStatus.TOO_MANY_REQUESTS),


    // SECURITY & AUTH MODULE (1001 - 1019)
    UNAUTHENTICATED(1001, "Unauthenticated. Please log in again", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1003, "Invalid phone number or password", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1004, "Session has expired. Please refresh your token", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1005, "Invalid token provider", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND(1006, "Refresh token not found or already revoked", HttpStatus.UNAUTHORIZED),
    INVALID_RESET_TOKEN(1007, "Invalid or expired password reset token", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(1008, "This reset token has already been used", HttpStatus.BAD_REQUEST),

    // USER MODULE - REGISTRATION & ACCOUNT STATUS (1020 - 1049)
    REGISTRATION_FAILED(1020, "Registration failed. Please check your information or contact support", HttpStatus.CONFLICT),
    PASSWORD_NOT_MATCH(1021, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_EXISTS(1022, "Phone number is already registered", HttpStatus.BAD_REQUEST),
    CITIZEN_ID_ALREADY_EXISTS(1023, "Citizen ID is already registered", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1024, "User profile not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND(1025, "Account not found", HttpStatus.NOT_FOUND),
    DEVICE_NOT_REGISTERED(1026, "Device is not registered", HttpStatus.NOT_FOUND),

    // ACCOUNT BANKING STATUS (1050 - 1099)
    ACCOUNT_INACTIVE(1050, "Your bank account is inactive", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1051, "Your account has been locked due to multiple failed login attempts", HttpStatus.LOCKED),
    PHONE_NOT_VERIFIED(1052, "Your phone number has not been verified via OTP", HttpStatus.FORBIDDEN),

    // Account status change errors (1053 - 1069)
    CANNOT_FREEZE_LAST_ACTIVE_ACCOUNT(1053, "Cannot freeze the last active account. You must have at least one active account", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_CLOSED(1054, "This account has already been closed", HttpStatus.BAD_REQUEST),
    ACCOUNT_CLOSED(1055, "Cannot perform operation on a closed account", HttpStatus.BAD_REQUEST),
    ACCOUNT_STATUS_NOT_CHANGEABLE(1056, "This account status cannot be changed by user", HttpStatus.FORBIDDEN),
    INVALID_ACCOUNT_STATUS(1057, "Invalid account status for this operation", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FROZEN(1058, "Cannot activate an account that is not frozen", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(1059, "Cannot freeze an account that is not active", HttpStatus.BAD_REQUEST),
    INVALID_ACTION(1060, "Invalid action specified", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_NUMBER(1061, "Invalid account number format", HttpStatus.BAD_REQUEST),
    ACCOUNT_SUSPENDED(1062, "Account has been suspended. Please contact support", HttpStatus.FORBIDDEN),
    INSUFFICIENT_BALANCE(4010, "Insufficient balance", HttpStatus.BAD_REQUEST),
    TRANSFER_SAME_ACCOUNT(4011, "Cannot transfer to the same account", HttpStatus.BAD_REQUEST),

    // LOCATION / REGION MODULE (1100 - 1199)
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