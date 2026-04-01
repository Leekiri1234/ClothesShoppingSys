package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.entities.cms.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    // Tìm danh sách để hiển thị ở Dropdown (Sắp xếp mới nhất lên đầu)
    List<NotificationRecipient> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    // Tìm danh sách phục vụ phân trang ở trang index.html
    Page<NotificationRecipient> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // Tìm các thông báo chưa đọc của 1 user cụ thể
    List<NotificationRecipient> findByAccountIdAndIsReadFalse(Long accountId);

    // Đếm số lượng chưa đọc để hiện số ở Chuông
    long countByAccountIdAndIsReadFalse(Long accountId);
}