package com.clothshop.domain.models.marketing;

import com.clothshop.domain.models.base.BaseEntity;
import com.clothshop.domain.models.product.Product;
import com.clothshop.domain.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class FlashSaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice; // Giá sau khi giảm
}
