package com.poly.ASM.dto.order;

import lombok.Data;

@Data
public class OrderStatusDTO {
    private String status;
    private Integer count;

    public OrderStatusDTO(String status, Integer count) {
        this.status = status;
        this.count = count;
    }
}
