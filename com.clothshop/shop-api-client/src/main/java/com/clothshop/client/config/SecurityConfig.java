package com.clothshop.client.config;

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
 * Spring Security configuration for Client module.
 * Uses Session-based authentication (NOT JWT).
 * CSRF protection is MANDATORY for session-based apps.
 *
 * Access Control:
 * - Public: /, /products/**, /search, /login, /register
 * - Customer-only: /profile/**, /cart/**, /checkout/**, /orders/** (requires ROLE_CUSTOMER)
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
    public SecurityConfig(@Qualifier("clientUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configure SecurityFilterChain for session-based authentication.
     */
    @Bean
    public SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Set custom UserDetailsService for authentication
                .userDetailsService(userDetailsService)

                // Session Management Configuration
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                // CSRF Protection
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/favicon.ico", "/error/**").permitAll()
                        .requestMatchers("/", "/home", "/products/**", "/search", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Customer-only pages
                        .requestMatchers("/profile/**", "/cart/**", "/checkout/**", "/orders/**").hasRole("CUSTOMER")

                        .anyRequest().permitAll()
                )

                // Form Login Configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Remember-Me Configuration
                .rememberMe(remember -> remember
                        .key("clothshop-client-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("clothshop-remember-me")
                )

                // Logout Configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "clothshop-remember-me")
                        .permitAll()
                );

        return http.build();
    }
}
