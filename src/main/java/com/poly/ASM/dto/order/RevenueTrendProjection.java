package com.poly.ASM.dto.order;

import java.math.BigDecimal;

public interface RevenueTrendProjection {
    Integer getMonth();
    BigDecimal getRevenue();
    Integer getTotalOrders();
}
