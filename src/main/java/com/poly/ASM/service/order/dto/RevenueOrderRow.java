package com.poly.ASM.service.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RevenueOrderRow {

    private Long orderId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
    private LocalDateTime orderCreateDate;
    private String categoryName;

    private boolean firstRow;
    private int rowSpan;
    private int orderIndex;
}
