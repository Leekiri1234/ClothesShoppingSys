package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Flash Sale Items - Sản phẩm tham gia flash sale với giá ưu đãi.
 */
@Entity
@Table(name = "flash_sale_items")
@SQLDelete(sql = "UPDATE flash_sale_items SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "flash_sale_item_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlashSaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "discount_type", length = 20)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice; // Giá sau khi giảm
}
