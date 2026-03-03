package com.clothshop.domain.entities.marketing;

import com.clothshop.domain.entities.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flash Sale - Chương trình giảm giá trong thời gian ngắn.
 */
@Entity
@Table(name = "flash_sales")
@SQLDelete(sql = "UPDATE flash_sales SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "flash_sale_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class FlashSale extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "status", length = 20)
    private String status; // UPCOMING, ONGOING, ENDED

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlashSaleItem> flashSaleItems;
}
