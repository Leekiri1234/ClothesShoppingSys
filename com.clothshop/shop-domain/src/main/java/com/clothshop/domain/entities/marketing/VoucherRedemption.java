package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.order.Order;
import com.clothshop.domain.entities.auth.Customer;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "voucher_redemptions")
@SQLDelete(sql = "UPDATE voucher_redemptions SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "redemption_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class VoucherRedemption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount; // Số tiền thực tế được giảm
}