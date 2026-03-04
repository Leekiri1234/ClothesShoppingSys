package com.clothshop.admin.services;

import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Role;
import com.clothshop.domain.entities.auth.Staff;
import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.enums.AccountType;
import com.clothshop.domain.enums.StaffRole;
import com.clothshop.domain.repositories.auth.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
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
 * Custom UserDetailsService for Admin Module.
 * Only allows STAFF accounts to login.
 * Loads StaffRole enum from Role entity for authorization.
 */
@Service("adminUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * Locates the STAFF user based on username or email.
     * Only STAFF accounts with active status can login to Admin module.
     *
     * @param usernameOrEmail the username or email identifying the user
     * @return UserDetails object containing user information and staff role
     * @throws UsernameNotFoundException if user not found, inactive, or not a STAFF account
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find by username first, then by email (with JOIN FETCH Staff and Role)
        Account account = accountRepository.findByUsernameWithStaffAndRole(usernameOrEmail)
                .orElseGet(() -> accountRepository.findByEmailWithStaffAndRole(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "Staff account not found or inactive: " + usernameOrEmail)));

        // Admin module only allows STAFF accounts
        if (account.getAccountType() != AccountType.STAFF) {
            throw new UsernameNotFoundException(
                    "Access denied: Only staff accounts can access admin portal");
        }

        if (account.getAccountStatus() == AccountStatus.LOCKED) {
            throw new DisabledException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Super Admin.");
        }

        // 2. Build authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));

        if (account.getStaff() != null && account.getStaff().getRole() != null) {
            StaffRole staffRole = account.getStaff().getRole().getStaffRole();
            if (staffRole != null) {
                // Nếu DB là SUPER_ADMIN -> Authority sẽ là ROLE_SUPER_ADMIN
                authorities.add(new SimpleGrantedAuthority("ROLE_" + staffRole.name()));
            }
        }

        // Return Spring Security's User object
        // IMPORTANT: Only store minimal user info, not the entire Account entity
        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!account.getIsActive())
                .build();
    }
}
