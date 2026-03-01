package com.clothshop.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Domain Configuration for shared infrastructure beans.
 *
 * Architecture Note:
 * - This PasswordEncoder bean is shared across all modules (admin, client)
 * - It's placed in shop-domain because seeding logic needs it
 * - BCryptPasswordEncoder is used for consistent password hashing
 */
@Configuration
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
}

