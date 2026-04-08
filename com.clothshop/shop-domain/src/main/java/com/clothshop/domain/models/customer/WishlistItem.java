package com.clothshop.domain.models.customer;

import com.clothshop.domain.models.base.BaseEntity;
import com.clothshop.domain.models.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"wishlist_id", "product_id"})
})
@SQLDelete(sql = "UPDATE wishlist_items SET is_active = false WHERE item_id = ?")
//@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "item_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class WishlistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
