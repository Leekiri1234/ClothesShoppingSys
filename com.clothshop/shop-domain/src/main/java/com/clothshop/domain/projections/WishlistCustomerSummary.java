package com.clothshop.domain.projections;

import java.time.LocalDateTime;

public interface WishlistCustomerSummary {
    Long getCustomerId();
    String getCustomerName();
    String getCustomerEmail();
    LocalDateTime getWishlistedAt();
}
