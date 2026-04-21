package com.poly.ASM.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Integer id,
        Integer productId,
        String customerId,
        String adminId,
        String senderRole,
        String content,
        String mediaUrl,
        LocalDateTime createdAt,
        String assignedAdminFullname
) {
}

