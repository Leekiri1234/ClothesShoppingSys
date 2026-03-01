package com.clothshop.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security configuration for Admin module.
 * Uses Session-based authentication (NOT JWT).
 * CSRF protection is MANDATORY for session-based apps.
 *
 * Access Control:
 * - /admin/** requires one of 4 staff roles:
 *   1. SUPER_ADMIN (full system control)
 *   2. MARKETING_STAFF (vouchers, banners, collections)
 *   3. SALE_PRODUCT_STAFF (products, inventory, orders)
 *   4. CUSTOMER_SERVICE (payments, RMA, customer management)
 * - Session timeout: 30 minutes
 * - Max concurrent sessions: 1 per user
 * - Session fixation protection: enabled
 * - Remember-Me: 7 days
 *
 * Note: PasswordEncoder bean is provided by DomainConfig in shop-domain module.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    // Constructor with @Qualifier to inject the correct bean
    public SecurityConfig(@Qualifier("adminUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configure SecurityFilterChain for session-based authentication.
     */
    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Set custom UserDetailsService for authentication
            .userDetailsService(userDetailsService)

            // Session Management Configuration
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession() // Protection against session fixation attacks
                .maximumSessions(1) // Limit to 1 concurrent session per user
                .maxSessionsPreventsLogin(false) // New login invalidates old session
            )

            // CSRF Protection (MANDATORY for session-based apps)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("STAFF", "SUPER_ADMIN", "MARKETING_STAFF",
                                                         "SALE_PRODUCT_STAFF", "CUSTOMER_SERVICE")
                .anyRequest().authenticated()
            )

            // Form Login Configuration
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error")
                .permitAll()
            )

            // Remember-Me Configuration (Optional)
            .rememberMe(remember -> remember
                .key("clothshop-admin-remember-me-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("clothshop-admin-remember-me")
            )

            // Logout Configuration
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
