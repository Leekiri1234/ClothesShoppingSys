package com.clothshop.common.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for SSR (Server-Side Rendering) with Thymeleaf.
 * Handles all exceptions and returns appropriate error pages.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business exceptions thrown from service layer.
     * Logs error and returns error page with error details.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        log.error("Business error: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("code", ex.getErrorCode().getCode());
        return "error/error-page";
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Extracts field errors and returns validation error page.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException ex, Model model) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Validation error: {}", fieldErrors);
        model.addAttribute("errors", fieldErrors);
        return "error/validation-error";
    }

    /**
     * Handles all uncaught exceptions.
     * Logs error and returns generic 500 error page.
     */
//    @ExceptionHandler(Exception.class)
//    public String handleUnexpectedException(Exception ex, Model model) {
//        log.error("Unexpected error", ex);
//        model.addAttribute("error", "An unexpected error occurred");
//        model.addAttribute("code", 500);
//        return "error/500";
//    }
}