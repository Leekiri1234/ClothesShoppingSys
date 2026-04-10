package com.clothshop.client.controllers;

import com.clothshop.client.dtos.request.RegisterRequest;
import com.clothshop.client.services.CustomerAuthService;
import com.clothshop.common.exceptions.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for authentication operations (login, registration).
 * Follows Traditional MVC (SSR) pattern with Thymeleaf.
 *
 * Endpoints:
 * - GET  /login     - Display login form
 * - GET  /register  - Display registration form
 * - POST /register  - Process registration
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CustomerAuthController {

    private final CustomerAuthService authService;

    /**
     * Display login page.
     * Spring Security handles actual authentication.
     *
     * @param error Optional error parameter from failed login
     * @param logout Optional logout parameter
     * @param model Model for Thymeleaf
     * @return login page view
     */
    @GetMapping("/login")
    public String showLoginPage(String error, String logout, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "client/auth/login";
    }

    /**
     * Display registration page.
     *
     * @param model Model for Thymeleaf
     * @return registration page view
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "client/auth/register";
    }

    /**
     * Process customer registration.
     *
     * Validation Flow:
     * 1. JSR-303 bean validation (via @Valid)
     * 2. Business validation in AuthService (unique username, unique email)
     * 3. Password BCrypt hashing
     *
     * @param request Registration form data
     * @param bindingResult Validation result
     * @param redirectAttributes Flash attributes for redirect
     * @return redirect to login on success, back to form on error
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Check for validation errors from @Valid annotation
        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed: {}", bindingResult.getAllErrors());
            return "client/auth/register";
        }

        try {
            // Process registration (validates unique username/email, hashes password)
            authService.register(request);

            // Success: Redirect to login page with success message
            redirectAttributes.addFlashAttribute("success",
                "Registration successful! Please login with your credentials.");
            log.info("Registration successful for username: {}", request.getUsername());
            return "redirect:/login";

        } catch (BusinessException e) {
            // Business validation failed (e.g., username/email already exists)
            log.error("Registration failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "client/auth/register";
        } catch (Exception e) {
            // Unexpected error
            log.error("Unexpected error during registration: ", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "client/auth/register";
        }
    }

    /**
     * Display forgot password page.
     *
     * @return forgot password page view
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "client/auth/forgot-password";
    }

    /**
     * Process forgot password request (email verification).
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model, RedirectAttributes redirectAttributes) {
        log.info("Process forgot password for email: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập địa chỉ email");
            return "client/auth/forgot-password";
        }

        boolean exists = authService.checkEmailExists(email);
        if (!exists) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống");
            return "client/auth/forgot-password";
        }

        // Redirect to reset password page with email
        redirectAttributes.addAttribute("email", email);
        return "redirect:/reset-password";
    }

    /**
     * Display reset password page.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/forgot-password";
        }
        
        com.clothshop.client.dtos.request.ResetPasswordRequest request = new com.clothshop.client.dtos.request.ResetPasswordRequest();
        request.setEmail(email);
        model.addAttribute("resetPasswordRequest", request);
        return "client/auth/reset-password";
    }

    /**
     * Process reset password.
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordRequest") com.clothshop.client.dtos.request.ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Reset password attempt for email: {}", request.getEmail());

        if (bindingResult.hasErrors()) {
            return "client/auth/reset-password";
        }

        try {
            authService.resetPassword(request.getEmail(), request.getNewPassword(), request.getConfirmPassword());
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "client/auth/reset-password";
        } catch (Exception e) {
            log.error("Unexpected error during reset password: ", e);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.");
            return "client/auth/reset-password";
        }
    }
}

