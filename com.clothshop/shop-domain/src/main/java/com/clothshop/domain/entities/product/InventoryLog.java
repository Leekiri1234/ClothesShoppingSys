package com.clothshop.domain.entities.product;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.entities.auth.Staff;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inventory_logs")
@SQLDelete(sql = "UPDATE inventory_logs SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "log_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class InventoryLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff; // Nhân viên thực hiện thay đổi

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty; // Số lượng thay đổi (+/-)

    @Column(name = "new_stock", nullable = false)
    private Integer newStock; // Tồn kho mới sau thay đổi

    @Column(name = "reason", nullable = false, length = 100)
    private String reason; // RESTOCK, ORDER, RETURN, ADJUSTMENT

    @Lob
    @Column(name = "note")
    private String note;
}