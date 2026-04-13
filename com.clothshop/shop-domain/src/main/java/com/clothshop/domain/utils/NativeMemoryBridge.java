package com.clothshop.domain.utils;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeMemoryBridge {
    private static final Unsafe unsafe;
    private static final long SHARED_ADDRESS;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            // Cấp phát 8 byte để lưu ID của Notification (kiểu long)
            SHARED_ADDRESS = unsafe.allocateMemory(8);
            unsafe.putLong(SHARED_ADDRESS, 0L);
            log.info(">>> Native Memory Allocated at: 0x{}", Long.toHexString(SHARED_ADDRESS));
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo Native Memory", e);
        }
    }

    public static void notifyNewId(long id) {
        unsafe.putLong(SHARED_ADDRESS, id);
    }

    public static long getLatestId() {
        return unsafe.getLong(SHARED_ADDRESS);
    }

    public static long getSharedAddress() {
        return SHARED_ADDRESS;
    }
}