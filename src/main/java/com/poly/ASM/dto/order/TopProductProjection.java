package com.poly.ASM.dto.order;

import java.math.BigDecimal;

public interface TopProductProjection {
    String getName();
    Integer getTotalSold();
    BigDecimal getRevenue();
}
