package com.nhatbui.digital_bank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    //SYSTEM / COMMON ERROR
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    //USER MODULE (1002 - 1009)
    REGISTRATION_FAILED(1001, "Registration failed. Please check your information or contact support", HttpStatus.CONFLICT),
    PASSWORD_NOT_MATCH(1002, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    PROVINCE_NOT_FOUND(1003, "Province not found", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "You do not have permission", HttpStatus.FORBIDDEN),

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
