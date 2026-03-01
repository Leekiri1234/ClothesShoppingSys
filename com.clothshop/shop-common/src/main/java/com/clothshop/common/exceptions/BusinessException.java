package com.clothshop.common.exceptions;

import lombok.Getter;

/**
 * Business exception for domain-specific errors.
 * Thrown by service layer when business rules are violated.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    /**
     * Constructor with ErrorCode only (uses ErrorCode's default message).
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    /**
     * Constructor with ErrorCode and custom message.
     * Allows overriding the default ErrorCode message.
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}