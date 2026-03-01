package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * CollectionItem - Bảng trung gian giữa Collection và Product
 * Theo ERD có thêm added_by và display_order
 */
@Entity
@Table(name = "collection_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"collection_id", "product_id"})
})
@SQLDelete(sql = "UPDATE collection_items SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "added_by", length = 50)
    private String addedBy; // Staff username
}
