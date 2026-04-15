package com.clothshop.domain.utils;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeMemoryBridge {
    private static final Unsafe unsafe;
    private static final long ID_ADDRESS;
    private static final long TITLE_ADDRESS;
    private static final int TITLE_MAX_SIZE = 256; // Giới hạn độ dài tiêu đề

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);

            // 1. Cấp phát 8 byte cho ID
            ID_ADDRESS = unsafe.allocateMemory(8);
            unsafe.putLong(ID_ADDRESS, 0L);

            // 2. Cấp phát 256 byte cho Title
            TITLE_ADDRESS = unsafe.allocateMemory(TITLE_MAX_SIZE);
            unsafe.setMemory(TITLE_ADDRESS, TITLE_MAX_SIZE, (byte) 0); // Clear vùng nhớ

            log.info(">>> Native Memory Allocated - ID: 0x{}, Title: 0x{}",
                    Long.toHexString(ID_ADDRESS), Long.toHexString(TITLE_ADDRESS));
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo Native Memory", e);
        }
    }

    // Cập nhật cả ID và Title vào RAM vật lý
    public static void update(long id, String title) {
        // Ghi ID
        unsafe.putLong(ID_ADDRESS, id);

        // Ghi Title (Chuyển chuỗi thành mảng byte)
        if (title != null) {
            byte[] bytes = title.getBytes(StandardCharsets.UTF_8);
            int length = Math.min(bytes.length, TITLE_MAX_SIZE - 1);

            for (int i = 0; i < length; i++) {
                unsafe.putByte(TITLE_ADDRESS + i, bytes[i]);
            }
            unsafe.putByte(TITLE_ADDRESS + length, (byte) 0); // Null-terminator như C++
        }
    }

    public static long getLatestId() {
        return unsafe.getLong(ID_ADDRESS);
    }

    // Đọc Title từ RAM vật lý và chuyển về String Java
    public static String getLatestTitle() {
        byte[] bytes = new byte[TITLE_MAX_SIZE];
        int i = 0;
        byte b;
        while ((b = unsafe.getByte(TITLE_ADDRESS + i)) != 0 && i < TITLE_MAX_SIZE - 1) {
            bytes[i] = b;
            i++;
        }
        return new String(bytes, 0, i, StandardCharsets.UTF_8);
    }

    public static long getSharedAddress() {
        return ID_ADDRESS;
    }
}