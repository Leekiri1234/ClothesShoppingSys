package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections")
@SQLDelete(sql = "UPDATE collections SET is_active = false WHERE collection_id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "collection_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Collection extends BaseEntity {

    @Column(name = "collection_name", nullable = false, length = 100)
    private String name;

    @Column(name = "collection_slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    //  Dùng LAZY để không kéo cả bảng trung gian lên khi query Collection
    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CollectionItem> items = new ArrayList<>();
}