package com.poly.ASM.dto.chat;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record ChatConversationDto(
        String customerId,
        String customerFullname,
        String customerPhoto,
        Integer productId,
        String productName,
        String productImage,
        BigDecimal productPrice,
        BigDecimal productDiscount,
        BigDecimal productFinalPrice,
        String categoryName,
        String adminId,
        String assignedAdminFullname,
        boolean lockedByOtherAdmin,
        String lastText,
        LocalDateTime lastAt
) {
}
