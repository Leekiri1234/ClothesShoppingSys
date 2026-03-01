package com.clothshop.domain.entities.cms;

import com.clothshop.domain.entities.base.BaseEntity;
import com.clothshop.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notifications")
@SQLDelete(sql = "UPDATE notifications SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@AttributeOverride(name = "id", column = @Column(name = "notification_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "target_type", length = 20)
    private NotificationType targetType; // SYSTEM, PROMOTION

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt; // Thời gian gửi thông báo

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Một thông báo có thể gửi cho nhiều người (Broadcasting)
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NotificationRecipient> recipients;
}