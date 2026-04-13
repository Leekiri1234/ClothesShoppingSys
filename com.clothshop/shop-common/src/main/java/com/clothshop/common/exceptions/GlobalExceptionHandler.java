package com.clothshop.common.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for SSR (Server-Side Rendering) with Thymeleaf.
 * Handles all exceptions and returns appropriate error pages with correct HTTP status codes.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business exceptions thrown from service layer.
     * Returns HTTP 400 and error page with error details.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model, HttpServletResponse response) {
        log.error("Business error: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("code", ex.getErrorCode().getCode());
        return "error/400";
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns HTTP 400 with field validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException ex, Model model, HttpServletResponse response) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Validation error: {}", fieldErrors);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("errors", fieldErrors);
        model.addAttribute("error", "Validation failed");
        return "error/400";
    }

    /**
     * Handles 404 Not Found exceptions.
     * Chỉnh sửa: Sử dụng Exception chung hoặc bắt riêng để tránh lỗi "No suitable resolver"
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public String handleNotFound(Exception ex, Model model, HttpServletResponse response) {
        String requestURL = "";

        // Kiểm tra xem lỗi cụ thể là gì để lấy URL cho đúng
        if (ex instanceof NoHandlerFoundException) {
            requestURL = ((NoHandlerFoundException) ex).getRequestURL();
        } else if (ex instanceof NoResourceFoundException) {
            requestURL = ((NoResourceFoundException) ex).getResourcePath();
        }

        log.warn("Resource not found: {}", requestURL);

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("error", "Resource not found");
        model.addAttribute("path", requestURL);

        return "error/404";
    }

    /**
     * Handles all uncaught exceptions.
     * Returns HTTP 500 error page.
     */
    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception ex, Model model, HttpServletResponse response) {
        log.error("Unexpected error", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        model.addAttribute("code", 500);
        return "error/500";
    }
}