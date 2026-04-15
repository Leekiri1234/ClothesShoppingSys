package com.clothshop.domain.listeners;

import com.clothshop.domain.models.cms.Notification;
import com.clothshop.domain.utils.NativeMemoryBridge;
import jakarta.persistence.PostPersist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@Slf4j
@Component
public class NotificationPropagationListener {

    @PostPersist
    public void onPostPersist(Notification notification) {
        // Tín hiệu này sẽ tự động được trigger ở cả 2 JVM (Admin & Client)
        // nhờ cơ chế JDBC Loopback Sync
        log.info("JDBC Sync Signal: Đã phát hiện thông báo mới ID: {}", notification.getId());
        NativeMemoryBridge.update(notification.getId(), notification.getTitle());        // Tại đây, bạn có thể gọi đến một Service xử lý UI
        // ví dụ: NotificationUIService.pushToBrowser(notification);
    }
}