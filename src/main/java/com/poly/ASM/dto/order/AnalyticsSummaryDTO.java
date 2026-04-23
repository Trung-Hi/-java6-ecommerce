package com.poly.ASM.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyticsSummaryDTO {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalCustomers;
    private Integer totalProducts;

    public AnalyticsSummaryDTO(BigDecimal totalRevenue, Integer totalOrders, Integer totalCustomers, Integer totalProducts) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalOrders = totalOrders != null ? totalOrders : 0;
        this.totalCustomers = totalCustomers != null ? totalCustomers : 0;
        this.totalProducts = totalProducts != null ? totalProducts : 0;
    }
}
