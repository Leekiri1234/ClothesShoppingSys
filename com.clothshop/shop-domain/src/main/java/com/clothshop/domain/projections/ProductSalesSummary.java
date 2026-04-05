package com.clothshop.domain.projections;

import java.math.BigDecimal;

public interface ProductSalesSummary {
    Long getProductId();
    String getProductName();
    Long getQuantity();
    BigDecimal getRevenue();
}
