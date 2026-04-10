package com.clothshop.common.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    public static String makeSlug(String input) {
        if (input == null || input.isEmpty()) return "";

        // Normalizer không xử lý được chữ 'đ', phải replace thủ công
        String result = input.replace("đ", "d").replace("Đ", "D");

        // Chuyển khoảng trắng thành gạch ngang trước
        result = WHITESPACE.matcher(result).replaceAll("-");

        // Normalize để tách dấu (ví dụ: á -> a + dấu sắc)
        result = Normalizer.normalize(result, Normalizer.Form.NFD);

        // Loại bỏ các ký tự không phải Latin/Số/Gạch ngang
        result = NONLATIN.matcher(result).replaceAll("");

        // Chuyển về chữ thường và cắt bỏ gạch ngang thừa ở 2 đầu
        return result.toLowerCase(Locale.ENGLISH).replaceAll("^-+|-+$", "");
    }
}