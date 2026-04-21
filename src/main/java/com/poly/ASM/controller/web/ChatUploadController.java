package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.exception.ApiException;
import com.poly.ASM.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatUploadController {

    private final AuthService authService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<?>> upload(@RequestParam("file") MultipartFile file) {
        if (authService.getUser() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File không hợp lệ");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String fileName = "chat-" + UUID.randomUUID() + ext;
        Path uploadDir = Path.of("uploads", "chat_pic");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(fileName), file.getBytes());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu ảnh");
        }
        String mediaUrl = "/images/chat_pic/" + fileName;
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Upload thành công", Map.of("mediaUrl", mediaUrl)));
    }
}

