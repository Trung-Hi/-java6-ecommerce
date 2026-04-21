package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.dto.request.ForgotPasswordRequest;
import com.poly.ASM.dto.request.ResetPasswordRequest;
import com.poly.ASM.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller xử lý forgot password / reset password.
 * Các endpoint này là PUBLIC (không cần authentication).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * API 1: Gửi email reset password.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        log.info("Received forgot password request for email: {}", request.getEmail());
        
        String message = passwordResetService.sendResetPasswordEmail(request);
        
        // Trả về response chung chung để tránh email enumeration
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    /**
     * API 2: Verify token có hợp lệ không.
     * GET /api/auth/verify-token?token=xxx
     */
    @GetMapping("/verify-token")
    public ResponseEntity<ApiResponse<?>> verifyToken(
            @RequestParam("token") String token) {
        
        log.debug("Verifying token: {}", token.substring(0, Math.min(8, token.length())) + "...");
        
        boolean isValid = passwordResetService.verifyToken(token);
        
        Map<String, Object> data = new HashMap<>();
        data.put("valid", isValid);
        
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Token hợp lệ", data));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Token không hợp lệ hoặc đã hết hạn", data));
        }
    }

    /**
     * API 3: Reset password với token.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Received reset password request with token: {}", 
            request.getToken().substring(0, Math.min(8, request.getToken().length())) + "...");
        
        String message = passwordResetService.resetPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
