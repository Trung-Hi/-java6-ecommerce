package com.poly.ASM.dto.chat;

public record ChatSendRequest(
        Integer productId,
        String customerId,
        String content,
        String mediaUrl
) {
}

