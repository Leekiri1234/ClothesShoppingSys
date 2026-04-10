package com.clothshop.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**")   // API dùng session auth, không cần CSRF token
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico", "/error/**").permitAll()
                        .requestMatchers("/", "/home", "/products/**", "/search", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/cart/add").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,  "/api/cart/**").hasRole("CUSTOMER")

                        .requestMatchers("/profile/**", "/cart/**", "/checkout/**",
                                "/orders/**", "/vouchers/**").hasRole("CUSTOMER")

                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .key("clothshop-client-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("clothshop-remember-me")
                )

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