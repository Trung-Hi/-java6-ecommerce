package com.poly.ASM.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyticsDTO {
    private Integer month;
    private BigDecimal revenue;
    private Integer totalOrders;

    public AnalyticsDTO(Integer month, BigDecimal revenue, Integer totalOrders) {
        this.month = month;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.totalOrders = totalOrders != null ? totalOrders : 0;
    }
}
