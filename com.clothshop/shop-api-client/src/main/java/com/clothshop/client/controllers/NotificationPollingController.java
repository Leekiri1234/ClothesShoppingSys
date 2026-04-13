package com.clothshop.client.controllers;

import com.clothshop.domain.utils.NativeMemoryBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotificationPollingController {

    @GetMapping("/api/sync/check")
    public Map<String, Object> checkUpdate(@RequestParam long lastId) {
        // Đọc trực tiếp từ địa chỉ RAM vật lý
        long currentId = NativeMemoryBridge.getLatestId();

        if (currentId > lastId) {
            // Nếu có ID mới, bạn có thể lấy thêm thông tin từ DB hoặc trả về luôn ID
            return Map.of("newId", currentId, "status", "UPDATE_FOUND");
        }
        return Map.of("newId", lastId, "status", "NO_CHANGE");
    }
}
