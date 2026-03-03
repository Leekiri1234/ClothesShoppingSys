package com.clothshop.domain.entities.product;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "categories", indexes = @Index(name = "idx_category_slug", columnList = "category_slug"))
@SQLDelete(sql = "UPDATE categories SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "category_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_slug", unique = true, nullable = false, length = 100)
    private String categorySlug;

    @Column(name = "cat_status", length = 20)
    private String catStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children;
}