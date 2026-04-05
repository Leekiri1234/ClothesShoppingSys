package com.clothshop.domain.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueSummary {
    LocalDate getDate();
    BigDecimal getRevenue();
    Long getOrderCount();
}
