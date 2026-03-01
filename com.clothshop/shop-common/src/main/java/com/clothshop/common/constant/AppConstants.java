package com.clothshop.common.constant;

/**
 * Application-wide constants.
 * Note: For status/type values, use Enums instead (type-safe approach).
 * This class contains only configuration values and technical constants.
 */
public final class AppConstants {

    // ========== Pagination Configuration ==========
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // ========== Date & Time Format ==========
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    // ========== Session Attributes (HttpSession keys) ==========
    public static final String SESSION_CART_ID = "CART_ID";
    public static final String SESSION_USER_ID = "USER_ID";
    public static final String SESSION_CUSTOMER_INFO = "CUSTOMER_INFO";

    // ========== Payment Methods (Business Configuration) ==========
    // Note: These are configuration values, not enum candidates
    public static final String PAYMENT_METHOD_COD = "COD";
    public static final String PAYMENT_METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String PAYMENT_METHOD_VNPAY = "VNPAY";
    public static final String PAYMENT_METHOD_MOMO = "MOMO";

    // ========== Product Configuration ==========
    public static final int PRODUCT_NAME_MAX_LENGTH = 255;
    public static final int PRODUCT_DESCRIPTION_MAX_LENGTH = 5000;
    public static final int MAX_PRODUCT_IMAGES = 10;

    // ========== Order Configuration ==========
    public static final String ORDER_INVOICE_PREFIX = "ORD-";
    public static final int ORDER_INVOICE_LENGTH = 12;

    // ========== Voucher Configuration ==========
    public static final int VOUCHER_CODE_LENGTH = 10;
    public static final int MAX_VOUCHER_USAGE_PER_CUSTOMER = 1;

    // ========== File Upload Configuration ==========
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    // ========== Cache Keys (if using Redis/Cache) ==========
    public static final String CACHE_PRODUCT_PREFIX = "product:";
    public static final String CACHE_CATEGORY_PREFIX = "category:";
    public static final int CACHE_TTL_SECONDS = 3600; // 1 hour

    // ========== API Response Messages ==========
    public static final String MSG_SUCCESS = "Operation completed successfully";
    public static final String MSG_NOT_FOUND = "Resource not found";
    public static final String MSG_UNAUTHORIZED = "Unauthorized access";
    public static final String MSG_FORBIDDEN = "Access forbidden";
    public static final String MSG_BAD_REQUEST = "Invalid request";

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
