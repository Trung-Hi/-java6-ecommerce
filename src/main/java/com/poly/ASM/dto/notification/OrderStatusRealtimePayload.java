package com.poly.ASM.dto.notification;

public record OrderStatusRealtimePayload(
        Long orderId,
        String status,
        String statusLabel
) {
}
