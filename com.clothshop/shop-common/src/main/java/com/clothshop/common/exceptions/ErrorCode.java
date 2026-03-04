package com.clothshop.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of all business error codes.
 * Each code has a numeric code, message, and HTTP status.
 */
@Getter
public enum ErrorCode {
    // System errors
    UNCATEGORIZED_EXCEPTION(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid error key", HttpStatus.BAD_REQUEST),

    // Authentication & Authorization errors
    UNAUTHENTICATED(1006, "Not authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "Unauthorized access", HttpStatus.UNAUTHORIZED),

    // Validation errors
    VALIDATION_ERROR(400, "Validation failed", HttpStatus.BAD_REQUEST),
    OPERATION_NOT_ALLOWED(403, "Operation not allowed", HttpStatus.FORBIDDEN),

    // Resource errors
    RESOURCE_NOT_FOUND(404, "Resource not found", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE(409, "Resource already exists", HttpStatus.CONFLICT),

    // User errors
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(1003, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1004, "Email already exists", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH(1007, "Password and confirmation do not match", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1008, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),

    // Product & Inventory errors
    PRODUCT_NOT_FOUND(2001, "Product not found", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(2002, "Product out of stock", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(2003, "Insufficient stock", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}