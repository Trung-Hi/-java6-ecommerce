package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Pattern CHAT_CTX_PATTERN = Pattern.compile("\\[CHAT_CTX\\]customerId=([^;\\n]+);productId=(\\d+)");

    private final AuthService authService;
    private final NotificationService notificationService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<?>> latest(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        Account user = authService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Vui lòng đăng nhập", null));
        }
        int size = Math.max(1, Math.min(limit, 30));
        var rows = notificationService.getLatest(user.getUsername(), size).stream().map(this::toNotificationData).toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", rows));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<?>> unreadCount() {
        Account user = authService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Vui lòng đăng nhập", null));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.countUnread(user.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Lấy số thông báo chưa đọc thành công", data));
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<ApiResponse<?>> read(@PathVariable("id") Long id) {
        Account user = authService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Vui lòng đăng nhập", null));
        }

        Optional<Notification> notificationOpt = notificationService.findByIdAndUsername(id, user.getUsername());
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy thông báo", null));
        }

        Notification notification = notificationOpt.get();
        if (notification.getRead() == null || !notification.getRead()) {
            notificationService.markRead(notification);
        }

        String redirectUrl = resolveRedirectUrl(notification);
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notification.getId());
        data.put("orderId", notification.getOrder() == null ? null : notification.getOrder().getId());
        data.put("redirectUrl", redirectUrl);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", data));
    }

    private Map<String, Object> toNotificationData(Notification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("content", stripChatContext(notification.getContent()));
        map.put("read", notification.getRead());
        map.put("createdAt", notification.getCreatedAt());
        Long orderId = notification.getOrder() == null ? null : notification.getOrder().getId();
        map.put("orderId", orderId);
        map.put("redirectUrl", resolveRedirectUrl(notification));
        return map;
    }

    private String resolveRedirectUrl(Notification notification) {
        if (notification.getOrder() != null && notification.getOrder().getId() != null) {
            if (authService.hasRole("ADMIN")) {
                return "/admin/order?orderId=" + notification.getOrder().getId();
            }
            return "/order/order-detail?id=" + notification.getOrder().getId();
        }
        Matcher matcher = CHAT_CTX_PATTERN.matcher(String.valueOf(notification.getContent()));
        if (matcher.find() && authService.hasRole("ADMIN")) {
            String customerId = matcher.group(1);
            String productId = matcher.group(2);
            return "/admin/chat?customerId=" + customerId + "&productId=" + productId;
        }
        return "/home/index";
    }

    private String stripChatContext(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        return content.replaceAll("\\n?\\[CHAT_CTX\\][\\s\\S]*$", "").trim();
    }
}
