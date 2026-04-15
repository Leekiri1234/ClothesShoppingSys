package com.clothshop.domain.repositories.cms;

import com.clothshop.domain.models.cms.Notification;
import com.clothshop.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType targetType);
}
