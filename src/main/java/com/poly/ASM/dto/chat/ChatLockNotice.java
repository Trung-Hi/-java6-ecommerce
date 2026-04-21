package com.poly.ASM.dto.chat;

public record ChatLockNotice(
        Integer productId,
        String customerId,
        String assignedAdminFullname,
        String message
) {
}

