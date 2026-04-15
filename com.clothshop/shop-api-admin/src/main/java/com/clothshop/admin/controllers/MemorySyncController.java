package com.clothshop.admin.controllers;

import com.clothshop.domain.utils.NativeMemoryBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MemorySyncController {

    @GetMapping("/api/sync/pointer")
    public Map<String, Object> getPointer() {
        // Trả về địa chỉ RAM cho Frontend
        return Map.of("address", NativeMemoryBridge.getSharedAddress());
    }
}