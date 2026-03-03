package com.clothshop.domain.entities.product;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = @Index(name = "idx_variant_sku", columnList = "sku"))
@SQLDelete(sql = "UPDATE product_variants SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "variant_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class ProductVariant extends BaseEntity {

    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku; // PROD_{id}_COL{color}_SZ{size}

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size_value", length = 20)
    private String sizeValue;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "retail_price", precision = 10, scale = 2)
    private BigDecimal retailPrice; // Giá bán lẻ cho variant này

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}