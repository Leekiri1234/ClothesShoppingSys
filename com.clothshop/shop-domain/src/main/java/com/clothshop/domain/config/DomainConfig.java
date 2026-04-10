package com.clothshop.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Domain Configuration for shared infrastructure beans.
 *
 * Architecture Note:
 * - This PasswordEncoder bean is shared across all modules (admin, client)
 * - It's placed in shop-domain because seeding logic needs it
 * - BCryptPasswordEncoder is used for consistent password hashing
 * - JPA Auditing is enabled to automatically track createdBy and updatedBy
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class DomainConfig {

    /**
     * PasswordEncoder bean for password hashing.
     * Used by:
     * - DatabaseSeeder (for initial account seeding)
     * - Admin/Client modules (for authentication)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuditorAware bean for JPA Auditing.
     * Automatically provides the current username from SecurityContext
     * for @CreatedBy and @LastModifiedBy fields.
     *
     * Returns:
     * - Current authenticated username if available
     * - "SYSTEM" if no authentication context exists (e.g., during seeding)
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

            return Optional.of(authentication.getName());
        };
    }
}
