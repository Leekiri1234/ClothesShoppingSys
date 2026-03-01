package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.entities.cms.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    // Tìm thông báo chưa đọc của 1 user để hiện số badge trên UI
    long countByAccountIdAndIsReadFalse(Long accountId);

    // Lấy danh sách thông báo của user
    List<NotificationRecipient> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
