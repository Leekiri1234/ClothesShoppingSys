package com.clothshop.domain.repositories.order;

import com.clothshop.domain.models.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    // Lấy lịch sử theo đơn hàng, sắp xếp mới nhất lên đầu
    List<OrderStatusHistory> findByOrderIdOrderByIdDesc(Long orderId);
}