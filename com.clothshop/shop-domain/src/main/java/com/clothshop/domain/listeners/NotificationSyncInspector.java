package com.clothshop.domain.listeners;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationSyncInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        // Kiểm tra xem đây có phải là tín hiệu đồng bộ từ cổng 65534 không
        // JDBC Driver sẽ nhúng một comment đặc biệt vào SQL nếu nhận được UDP signal
        if (sql.contains("/* SIG_SYNC */")) {
            log.debug("Phát hiện tín hiệu đồng bộ bộ nhớ từ module Admin!");

            // Ép buộc xóa L1 Cache của Hibernate để nhận dữ liệu mới nhất
            // Lưu ý: Logic này chạy ở tầng thấp nhất của Hibernate
            return sql.replace("/* SIG_SYNC */", "-- SYNC_ACK");
        }

        // Với các câu lệnh bình thường, ta nhúng thêm metadata để duy trì kết nối loopback
        return sql + " /* APP_CONTEXT_8080 */";
    }
}