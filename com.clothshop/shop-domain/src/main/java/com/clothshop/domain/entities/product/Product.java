package com.clothshop.domain.entities.product;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.util.List;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_product_slug", columnList = "product_slug"))
@SQLDelete(sql = "UPDATE products SET is_active = false WHERE product_id = ?")
@AttributeOverride(name = "id", column = @Column(name = "product_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Product extends BaseEntity {

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_slug", unique = true, nullable = false, length = 200)
    private String productSlug;

    @Lob
    @Column(name = "product_desc")
    private String productDesc;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal basePrice;

    @Column(name = "prod_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ProductStatus prodStatus = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "category_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ProductImage> images;
}