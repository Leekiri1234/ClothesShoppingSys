package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "collections", indexes = @Index(name = "idx_collection_slug", columnList = "collection_slug"))
@SQLDelete(sql = "UPDATE collections SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "collection_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Collection extends BaseEntity {

    @Column(name = "collection_name", nullable = false, length = 100)
    private String collectionName;

    @Column(name = "collection_slug", unique = true, nullable = false, length = 100)
    private String collectionSlug;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // Quan hệ với CollectionItem (bảng trung gian)
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CollectionItem> collectionItems;
}