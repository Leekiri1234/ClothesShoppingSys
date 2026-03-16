package com.clothshop.client.services;

import com.clothshop.client.dtos.request.ProfileUpdateRequest;
import com.clothshop.client.dtos.response.CustomerProfileResponse;
import com.clothshop.client.mappers.CustomerProfileMapper;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.auth.Account;
import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.repositories.auth.AccountRepository;
import com.clothshop.domain.repositories.auth.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(String username) {
        return accountRepository.findByUsernameWithCustomer(username)
                .map(Account::getCustomer)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public void updateProfile(String username, ProfileUpdateRequest request) {
        Account account = accountRepository.findByUsernameWithCustomer(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED));

        Customer customer = account.getCustomer();
        // Update data từ request vào entity
        profileMapper.updateCustomerFromRequest(request, customer);
        customerRepository.save(customer);
    }
}