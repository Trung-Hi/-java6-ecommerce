package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
public class AdminChatRestController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<?>> conversations() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hội thoại thành công", chatService.getAdminConversations()));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<?>> messages(@RequestParam("customerId") String customerId,
                                                   @RequestParam("productId") Integer productId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử chat thành công", chatService.getAdminHistory(customerId, productId)));
    }
}
