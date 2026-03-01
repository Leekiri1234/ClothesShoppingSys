package com.clothshop.client.services;

import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.enums.AccountType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom UserDetailsService for Client Module.
 * Only allows CUSTOMER accounts to login.
 * Staff accounts should use the Admin portal.
 */
@Service("clientUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * Locates the CUSTOMER user based on username or email.
     * Only CUSTOMER accounts with active status can login to Client module.
     *
     * @param usernameOrEmail the username or email identifying the user
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user not found, inactive, or not a CUSTOMER account
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);

        // SỬA TẠI ĐÂY: Sử dụng method JOIN FETCH c (customer) để nạp sẵn dữ liệu khách hàng
        // Điều này chặn đứng việc Hibernate tự ý đi tìm Staff.
        Account account = accountRepository.findByUsernameWithCustomer(usernameOrEmail)
                .or(() -> accountRepository.findByEmailWithCustomer(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("User not found or inactive: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found or inactive: " + usernameOrEmail);
                });

        // Client module only allows CUSTOMER accounts
        if (account.getAccountType() != AccountType.CUSTOMER) {
            log.warn("Non-customer account attempted to login: {}", usernameOrEmail);
            throw new UsernameNotFoundException(
                    "Access denied: Only customer accounts can access this portal");
        }

        log.debug("Customer account found: username={}, status={}",
                account.getUsername(), account.getAccountStatus());

        // Build authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        log.info("Checking password for user: {}. Encoded password in DB: {}",
                account.getUsername(), account.getPassword());

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities)
                // isActive đã được check trong Query (WithCustomer)
                .accountLocked(account.getAccountStatus() != com.clothshop.domain.enums.AccountStatus.ACTIVE)
                .disabled(!account.getIsActive())
                .build();
    }
}
