package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
// Thêm UniqueConstraint để DB tự động chặn rác dữ liệu nếu có 2 luồng cùng gán 1 SP.
@Table(name = "collection_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_collection_product", columnNames = {"collection_id", "product_id"})
        })
@SQLDelete(sql = "UPDATE collection_items SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
// No @AttributeOverride needed - table uses 'id' column directly
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CollectionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "added_by", length = 50)
    private String addedBy; // Lưu username người gán
}