package com.clothshop.domain.utils;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class NativeMemoryBridge {
    private static final Unsafe unsafe;
    private static final long SHARED_ADDRESS;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            // Cấp phát 8 byte để lưu Long ID của Notification mới nhất
            SHARED_ADDRESS = unsafe.allocateMemory(8);
            unsafe.putLong(SHARED_ADDRESS, 0L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void notifyNewId(long id) {
        unsafe.putLong(SHARED_ADDRESS, id);
    }

    public static long getLatestId() {
        return unsafe.getLong(SHARED_ADDRESS);
    }
}