package com.clothshop.domain.projections;

public interface WishlistProductSummary {
    Long getProductId();
    String getProductName();
    String getCategoryName();
    Long getWishlistCount();
}
