package com.clothshop.domain.projections;

import java.time.LocalDate;

public interface WishlistTrendSummary {
    LocalDate getDate();
    Long getWishlistCount();
}
