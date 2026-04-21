package com.poly.ASM.dto.notification;

public record NotificationRealtimePayload(
        Long id,
        String title,
        String content,
        Long orderId,
        String link,
        boolean read,
        long unreadCount
) {
}
