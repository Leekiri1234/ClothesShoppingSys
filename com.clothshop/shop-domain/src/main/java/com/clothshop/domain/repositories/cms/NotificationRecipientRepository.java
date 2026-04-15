package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.models.cms.NotificationRecipient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    // 1. Dùng cho cả trang LIST (truyền pageable lớn) và DROPDOWN (truyền pageable 5)
    // Dùng JOIN FETCH để tránh lỗi "no Session" khi lấy title/content
    @Query("SELECT nr FROM NotificationRecipient nr " +
            "JOIN FETCH nr.notification n " +
            "WHERE nr.account.id = :accountId " +
            "ORDER BY nr.createdAt DESC")
    List<NotificationRecipient> findAllByAccountIdWithFetch(@Param("accountId") Long accountId, Pageable pageable);

    // 2. Tìm chi tiết 1 thông báo
    @Query("SELECT nr FROM NotificationRecipient nr " +
            "JOIN FETCH nr.notification n " +
            "WHERE nr.notification.id = :notificationId AND nr.account.id = :accountId")
    Optional<NotificationRecipient> findByNotificationIdAndAccountId(@Param("notificationId") Long notificationId, @Param("accountId") Long accountId);

    // 3. Hàm đếm (Số lượng) thông báo chưa đọc - Trả về Long mới đúng là "Đếm"
    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr " +
            "WHERE nr.account.id = :accountId AND nr.isRead = false")
    long countUnreadNotifications(@Param("accountId") Long accountId);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = CURRENT_TIMESTAMP WHERE nr.account.id = :accountId AND nr.isRead = false")
    void markAllAsRead(@Param("accountId") Long accountId);
}