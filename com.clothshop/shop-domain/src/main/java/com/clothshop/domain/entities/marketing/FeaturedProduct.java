package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "featured_products")
@SQLDelete(sql = "UPDATE featured_products SET is_active = false WHERE featured_prod_id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "featured_prod_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeaturedProduct extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}