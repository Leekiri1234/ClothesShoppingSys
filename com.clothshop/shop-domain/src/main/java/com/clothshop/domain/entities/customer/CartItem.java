package com.clothshop.domain.entities.customer;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.product.ProductVariant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "variant_id"})
})
@AttributeOverride(name = "id", column = @Column(name = "cart_item_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price; // Snapshot giá tại thời điểm thêm vào giỏ
}