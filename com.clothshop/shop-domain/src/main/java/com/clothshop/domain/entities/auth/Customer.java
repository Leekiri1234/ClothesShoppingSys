package com.clothshop.domain.entities.auth;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "customer_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Customer extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(length = 100)
    private String email; // Email riêng của customer (có thể khác với Account)

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Lob
    @Column(name = "address", length = 255)
    private String address;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;
}