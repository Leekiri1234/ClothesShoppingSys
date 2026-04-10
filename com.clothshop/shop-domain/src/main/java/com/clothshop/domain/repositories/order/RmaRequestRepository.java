package com.clothshop.domain.repositories.order;

import com.clothshop.domain.models.order.RmaRequest;
import com.clothshop.domain.enums.RmaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RmaRequestRepository extends JpaRepository<RmaRequest, Long>, JpaSpecificationExecutor<RmaRequest> {

    /**
     * Lấy chi tiết yêu cầu RMA.
     * Tối ưu: Lấy kèm Order (invoice), Customer và danh sách sản phẩm của đơn đó.
     */
    @EntityGraph(attributePaths = {"order", "customer", "order.orderItems"})
    @Override
    Optional<RmaRequest> findById(Long id);

    /**
     * Lọc danh sách RMA theo trạng thái.
     */
    @EntityGraph(attributePaths = {"order", "customer"})
    Page<RmaRequest> findByStatus(RmaStatus status, Pageable pageable);

    /**
     * Tìm các yêu cầu RMA theo mã hóa đơn (orderInvoice).
     */
    @EntityGraph(attributePaths = {"order", "customer"})
    Page<RmaRequest> findByOrder_OrderInvoiceContainingIgnoreCase(String orderInvoice, Pageable pageable);

    /**
     * Kiểm tra xem yêu cầu RMA có tồn tại cho Order và Customer này không.
     */
    boolean existsByOrderIdAndCustomerId(Long orderId, Long customerId);

    /**
     * Tìm yêu cầu RMA dựa trên ID của đơn hàng và ID của khách hàng.
     */
    @EntityGraph(attributePaths = {"order", "customer"})
    Optional<RmaRequest> findByOrderIdAndCustomerId(Long orderId, Long customerId);

    /**
     * Đếm số lượng yêu cầu theo trạng thái (để làm thông báo số yêu cầu mới).
     */
    long countByStatus(RmaStatus status);

    /**
     * TRUY VẤN TỔNG HỢP: Tìm theo Mã hóa đơn HOẶC Tên khách hàng.
     * Đã sửa orderCode -> orderInvoice cho khớp Entity.
     */
    @Query("SELECT r FROM RmaRequest r WHERE " +
            "LOWER(r.order.orderInvoice) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    @EntityGraph(attributePaths = {"order", "customer"})
    Page<RmaRequest> searchRma(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm tất cả yêu cầu RMA của một khách hàng.
     */
    @EntityGraph(attributePaths = {"order", "customer"})
    Page<RmaRequest> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Tìm yêu cầu RMA theo ID với đầy đủ thông tin.
     */
    @EntityGraph(attributePaths = {"order", "customer", "order.orderItems"})
    Optional<RmaRequest> findByIdAndCustomerId(Long rmaId, Long customerId);
}
