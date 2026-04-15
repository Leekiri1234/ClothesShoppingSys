package com.clothshop.admin.controllers; // Đổi package cho đúng bên Admin

import com.clothshop.domain.utils.NativeMemoryBridge;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080") // CỰC KỲ QUAN TRỌNG: Cho phép Client 8080 gọi sang
public class NotificationPollingController {

    @GetMapping("/api/sync/check")
    public Map<String, Object> checkUpdate(@RequestParam long lastId) {
        long currentId = NativeMemoryBridge.getLatestId();
        String title = NativeMemoryBridge.getLatestTitle();

        if (currentId > lastId) {
            return Map.of(
                    "newId", currentId,
                    "title", title,
                    "status", "UPDATE_FOUND"
            );
        }
        return Map.of("newId", lastId, "status", "NO_CHANGE");
    }
}