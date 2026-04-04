package com.clothshop.domain.entities.order;

import com.clothshop.domain.entities.auth.Customer;
import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.OrderStatus;
import com.clothshop.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_invoice", columnList = "order_invoice"),
        @Index(name = "idx_order_status", columnList = "order_status")
})
@SQLDelete(sql = "UPDATE orders SET is_active = false WHERE order_id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "order_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Order extends BaseEntity {

    @Column(name = "order_invoice", unique = true, nullable = false, length = 50)
    private String orderInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "total_quantity", nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    // Giá trước giảm
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    // Giá cuối cùng khách phải trả
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 20, nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 20) // Tối ưu: Load 20 đơn hàng chỉ tốn 1 query phụ lấy items
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("id DESC") // Luôn lấy lịch sử mới nhất lên đầu
    private List<OrderStatusHistory> statusHistory;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}