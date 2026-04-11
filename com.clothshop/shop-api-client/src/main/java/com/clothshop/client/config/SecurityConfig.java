package com.clothshop.client.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security configuration for Client module.
 * Uses Session-based authentication (NOT JWT).
 * CSRF protection is MANDATORY for session-based apps.
 *
 * Spring Boot 3 / Spring Security 6 FIX:
 * - Mặc định Spring Security 6 dùng XorCsrfTokenRequestAttributeHandler,
 *   nó XOR-encode token trước khi ghi vào cookie.
 * - JS đọc cookie XSRF-TOKEN ra giá trị đã bị XOR → không khớp với
 *   giá trị Spring Security expect trong header → 403 Forbidden dù đã login.
 * - Fix: dùng CsrfTokenRequestAttributeHandler (raw, không XOR).
 *
 * Access Control:
 * - Public: /, /products/**, /search, /login, /register
 * - Customer-only: /profile/**, /cart/**, /checkout/**, /orders/**
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

    public SecurityConfig(@Qualifier("clientUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)

                // Session Management Configuration
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                // CSRF Protection — FIX SPRING SECURITY 6
                // CsrfTokenRequestAttributeHandler = raw token, không XOR
                // → JS đọc cookie XSRF-TOKEN ra đúng giá trị cần gửi trong header
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico", "/error/**").permitAll()
                        .requestMatchers("/", "/home", "/products/**", "/search", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/profile/**", "/cart/**", "/checkout/**", "/orders/**", "/vouchers/**").hasRole("CUSTOMER")
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
                )

                // FIX AJAX: Phân biệt AJAX và browser request khi chưa đăng nhập.
                // fetch() tự follow 302 redirect → JS không thể detect được.
                // Nếu có header X-Requested-With → trả 401 thay vì redirect.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestedWith = request.getHeader("X-Requested-With");
                            if ("XMLHttpRequest".equals(requestedWith)) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        return http.build();
    }
}