package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "featured_collections")
@SQLDelete(sql = "UPDATE featured_collections SET is_active = false WHERE featured_coll_id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "featured_coll_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeaturedCollection extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false, unique = true)
    private Collection collection;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl; // Ảnh bìa riêng khi hiển thị ngoài trang chủ

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}