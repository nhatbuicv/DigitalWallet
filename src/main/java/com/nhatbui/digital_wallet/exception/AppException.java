package com.nhatbui.digital_wallet.exception;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private boolean replayed;

    public AppException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, boolean replayed) {
        this.errorCode = errorCode;
        this.replayed = replayed;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isReplayed() {
        return replayed;
    }

    public void setReplayed(boolean replayed) {
        this.replayed = replayed;
    }
}
