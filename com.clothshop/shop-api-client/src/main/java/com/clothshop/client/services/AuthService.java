package com.clothshop.client.services;

import com.clothshop.client.dtos.request.RegisterRequest;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.enums.AccountType;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.auth.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations (registration, login).
 * Follows strict architectural guidelines:
 * - Validates business rules
 * - Uses BCrypt for password hashing
 * - Ensures 1 username per account, 1 email per account
 * - Creates both Account and Customer entities on registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new customer account.
     *
     * Business Rules:
     * 1. Username must be unique
     * 2. Email must be unique
     * 3. Password must match confirmation
     * 4. Password is BCrypt hashed before storage
     *
     * @param request Registration request DTO
     * @throws BusinessException if validation fails
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Processing registration for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Validation 1: Check if username already exists
        if (accountRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS,
                "Username '" + request.getUsername() + "' is already taken");
        }

        // Validation 2: Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS,
                "Email '" + request.getEmail() + "' is already registered");
        }

        // Validation 3: Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration failed: Password mismatch for username - {}", request.getUsername());
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // Validation 4: Check password length (additional check beyond @Size annotation)
        if (request.getPassword().length() < 8) {
            log.warn("Registration failed: Invalid password length for username - {}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Step 1: Create Account entity
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hashing
        account.setAccountType(AccountType.CUSTOMER);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCreatedBy("SYSTEM");
        account.setIsActive(true);

        // Save account first to get generated ID
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with ID: {} for username: {}", savedAccount.getId(), savedAccount.getUsername());

        // Step 2: Create Customer profile linked to Account
        Customer customer = new Customer();
        customer.setFullName(request.getFirstName() + " " + request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAccount(savedAccount);
        customer.setCreatedBy("SYSTEM");
        customer.setIsActive(true);

        customerRepository.save(customer);
        log.info("Customer profile created successfully for account ID: {}", savedAccount.getId());
    }

    /**
     * Check if a username is available for registration.
     *
     * @param username Username to check
     * @return true if available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        return !accountRepository.existsByUsername(username);
    }

    /**
     * Check if an email is available for registration.
     *
     * @param email Email to check
     * @return true if available, false if taken
     */
    public boolean isEmailAvailable(String email) {
        return !accountRepository.existsByEmail(email);
    }
}

