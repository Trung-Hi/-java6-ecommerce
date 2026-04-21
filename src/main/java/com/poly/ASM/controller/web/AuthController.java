package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.security.JwtCookieService;
import com.poly.ASM.security.JwtService;
import com.poly.ASM.security.TokenBlacklistService;
import com.poly.ASM.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/session")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/login-info")
    public ResponseEntity<ApiResponse<?>> loginInfo(@RequestParam(value = "error", required = false) String error) {
        String message = error == null ? "Sẵn sàng đăng nhập" : "Sai tài khoản hoặc mật khẩu";
        return ResponseEntity.ok(ApiResponse.success("Thông tin đăng nhập", Map.of("message", message)));
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        blacklistIfPresent(jwtCookieService.readAccessToken(request));
        blacklistIfPresent(jwtCookieService.readRefreshToken(request));
        jwtCookieService.clearAuthCookies(response);
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    private void blacklistIfPresent(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Date expiration = jwtService.extractExpiration(token);
            String username = jwtService.extractUsername(token);
            String tokenType = jwtService.extractTokenType(token);
            tokenBlacklistService.blacklist(
                    token,
                    Instant.ofEpochMilli(expiration.getTime()),
                    username,
                    tokenType != null ? tokenType.toUpperCase() : null
            );
        } catch (RuntimeException ignored) {
        }
    }
}
