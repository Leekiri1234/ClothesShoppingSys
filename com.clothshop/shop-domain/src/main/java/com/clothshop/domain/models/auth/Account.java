package com.clothshop.domain.models.auth;

import com.clothshop.domain.models.base.BaseEntity;
import com.clothshop.domain.enums.AccountStatus;
import com.clothshop.domain.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "accounts", indexes = @Index(name = "idx_account_email", columnList = "email"))
@AttributeOverride(name = "id", column = @Column(name = "account_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Account extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "account", orphanRemoval = true, fetch = FetchType.LAZY)
    private Customer customer;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "account", orphanRemoval = true, fetch = FetchType.LAZY)
    private Staff staff;
}
