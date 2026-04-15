package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.models.cms.NotificationRecipient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    // Lấy danh sách thông báo cho một Account ID cụ thể (Sử dụng cho cả Top 5 và Full List)
    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.account.id = :accountId ORDER BY nr.createdAt DESC")
    List<NotificationRecipient> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);

    // Tìm chi tiết thông báo cụ thể gắn với Account (Để bảo mật, tránh user xem được noti của người khác)
    @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.notification.id = :notificationId AND nr.account.id = :accountId")
    Optional<NotificationRecipient> findByNotificationIdAndAccountId(@Param("notificationId") Long notificationId, @Param("accountId") Long accountId);

    // Đếm số thông báo chưa đọc
    long countByAccountIdAndIsReadFalse(Long accountId);
}