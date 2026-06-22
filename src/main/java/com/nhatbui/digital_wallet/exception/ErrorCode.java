package com.nhatbui.digital_wallet.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // ─── SYSTEM / COMMON (9000 - 9999) ────────────────────────────────────
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized system error",                        HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR         (9000, "Internal server error",                             HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY            (9001, "Uncategorized error key defined",                   HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY   (9002, "Invalid request body or missing fields",            HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED    (9003, "Too many requests. Please try again later",         HttpStatus.TOO_MANY_REQUESTS),

    // ─── AUTH / SECURITY (1001 - 1019) ────────────────────────────────────
    UNAUTHENTICATED        (1001, "Unauthenticated. Please log in again",              HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED           (1002, "You do not have permission to access this resource",HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS    (1003, "Invalid phone number or password",                  HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED          (1004, "Session has expired. Please refresh your token",    HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN          (1005, "Invalid token provider",                            HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND(1006, "Refresh token not found or already revoked",        HttpStatus.UNAUTHORIZED),
    INVALID_RESET_TOKEN    (1007, "Invalid or expired password reset token",           HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED     (1008, "This reset token has already been used",            HttpStatus.BAD_REQUEST),

    // ─── USER MODULE (1020 - 1049) ────────────────────────────────────────
    REGISTRATION_FAILED    (1020, "Registration failed. Please check your information or contact support", HttpStatus.CONFLICT),
    PASSWORD_NOT_MATCH     (1021, "Password and confirm password do not match",        HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_EXISTS   (1022, "Phone number is already registered",                HttpStatus.BAD_REQUEST),
    CITIZEN_ID_ALREADY_EXISTS(1023, "Citizen ID is already registered",                HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND         (1024, "User not found",                                    HttpStatus.NOT_FOUND),
    DEVICE_NOT_REGISTERED  (1025, "Device is not registered",                          HttpStatus.NOT_FOUND),

    // ─── USER STATUS (1050 - 1099) ────────────────────────────────────────
    USER_LOCKED            (1050, "Your wallet has been locked due to multiple failed login attempts", HttpStatus.LOCKED),
    USER_INACTIVE           (1050, "Your wallet is inactive",                          HttpStatus.FORBIDDEN),
    PHONE_NOT_VERIFIED     (1051, "Your phone number has not been verified via OTP",   HttpStatus.FORBIDDEN),

    // ─── WALLET MODULE (2001 - 2099) ──────────────────────────────────────
    WALLET_NOT_FOUND       (2001, "Wallet not found",                                  HttpStatus.NOT_FOUND),
    WALLET_NOT_ACTIVE      (2002, "Wallet is not active",                              HttpStatus.BAD_REQUEST),
    WALLET_FROZEN          (2003, "Wallet has been frozen. Please contact support",    HttpStatus.FORBIDDEN),
    WALLET_CLOSED          (2004, "Wallet has been closed",                            HttpStatus.BAD_REQUEST),
    WALLET_SUSPENDED       (2005, "Wallet has been suspended. Please contact support", HttpStatus.FORBIDDEN),
    INVALID_WALLET_NUMBER  (2006, "Invalid wallet wallet number format",               HttpStatus.BAD_REQUEST),
    WALLET_STATUS          (2007, "Wallet status is invalid" ,                         HttpStatus.BAD_REQUEST),
    SYSTEM_WALLET_NOT_FOUND(2008, "System wallet not found" ,                          HttpStatus.NOT_FOUND),

    // Wallet status transitions
    CANNOT_FREEZE_LAST_ACTIVE_WALLET(2010, "Cannot freeze the last active wallet. You must have at least one active wallet", HttpStatus.BAD_REQUEST),
    WALLET_ALREADY_CLOSED  (2011, "This wallet has already been closed",               HttpStatus.BAD_REQUEST),
    WALLET_STATUS_NOT_CHANGEABLE(2012, "This wallet status cannot be changed by user", HttpStatus.FORBIDDEN),
    WALLET_NOT_FROZEN      (2013, "Cannot activate a wallet that is not frozen",       HttpStatus.BAD_REQUEST),
    INVALID_WALLET_STATUS  (2014, "Invalid wallet status for this operation",          HttpStatus.BAD_REQUEST),
    INVALID_ACTION         (2015, "Invalid action specified",                          HttpStatus.BAD_REQUEST),

    // ─── TRANSACTION MODULE (4001 - 4099) ─────────────────────────────────
    INSUFFICIENT_BALANCE   (4001, "Insufficient balance",                              HttpStatus.BAD_REQUEST),
    TRANSFER_SAME_WALLET   (4002, "Cannot transfer to the same wallet",                HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND  (4003, "Transaction not found",                             HttpStatus.NOT_FOUND),
    INVALID_DEPOSIT_AMOUNT (4004,"Invalid deposit amount",                             HttpStatus.BAD_REQUEST ),
    INVALID_WITHDRAW_AMOUNT (4005,"Invalid withdraw amount",                             HttpStatus.BAD_REQUEST ),

    // ─── LOCATION MODULE (5001 - 5099) ────────────────────────────────────
    PROVINCE_NOT_FOUND     (5001, "Province not found",                                HttpStatus.BAD_REQUEST),
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