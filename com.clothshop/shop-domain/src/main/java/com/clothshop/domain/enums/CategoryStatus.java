package com.clothshop.domain.enums;

/**
 * Category Status Enum
 * Business status for category entities
 */
public enum CategoryStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String displayName;

    CategoryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum name as String for database storage
     * @return "ACTIVE" or "INACTIVE"
     */
    public String getValue() {
        return this.name();
    }

    /**
     * Parse String to CategoryStatus enum
     * @param value String value from database
     * @return CategoryStatus enum
     */
    public static CategoryStatus fromValue(String value) {
        if (value == null) {
            return ACTIVE; // Default value
        }
        try {
            return CategoryStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE; // Fallback to default
        }
    }
}

