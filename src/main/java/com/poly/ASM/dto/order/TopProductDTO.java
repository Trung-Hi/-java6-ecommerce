package com.poly.ASM.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopProductDTO {
    private String name;
    private Integer totalSold;
    private BigDecimal revenue;

    public TopProductDTO(String name, Integer totalSold, BigDecimal revenue) {
        this.name = name;
        this.totalSold = totalSold;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
}
