package com.clothshop.client.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Global Error Controller for Client Portal
 * Handles HTTP error pages (404, 403, 400, 401, 500, 503, etc.)
 */
@Slf4j
@Controller
public class ClientErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        
        // Get the status code from the request
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object errorException = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String requestPath = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int code = (statusCode != null) ? Integer.parseInt(statusCode.toString()) : 500;
        String message = (errorMessage != null) ? errorMessage.toString() : "An error occurred";

        log.error("Error {} - {}: {}", code, message, requestPath);

        // Add attributes for error page
        model.addAttribute("code", code);
        model.addAttribute("error", message);
        model.addAttribute("path", requestPath);

        // Route to appropriate error page template
        switch (code) {
            case 400:
                return "error/400";
            case 401:
                return "error/401";
            case 403:
                return "error/403";
            case 404:
                return "error/404";
            case 503:
                return "error/503";
            case 500:
            default:
                return "error/500";
        }
    }

    public String getErrorPath() {
        return "/error";
    }
}
