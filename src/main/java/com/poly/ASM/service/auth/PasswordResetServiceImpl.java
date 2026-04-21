package com.poly.ASM.service.auth;

import com.poly.ASM.dto.request.ForgotPasswordRequest;
import com.poly.ASM.dto.request.ResetPasswordRequest;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.user.PasswordResetToken;
import com.poly.ASM.exception.ApiException;
import com.poly.ASM.repository.user.AccountRepository;
import com.poly.ASM.repository.user.PasswordResetTokenRepository;
import com.poly.ASM.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation của PasswordResetService.
 * Sử dụng UUID làm token, có expiry 15 phút, one-time use.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int MAX_TOKENS_PER_HOUR = 3; // Rate limiting

    @Override
    @Transactional
    public String sendResetPasswordEmail(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        // Tìm account theo email
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        
        if (accountOpt.isEmpty()) {
            // SECURITY: Không reveal thông tin - vẫn trả về success message
            // để tránh email enumeration attack
            log.warn("Email không tồn tại trong hệ thống: {}", email);
            return "Nếu email tồn tại, bạn sẽ nhận được link reset mật khẩu trong ít phút.";
        }

        Account account = accountOpt.get();
        
        // Kiểm tra tài khoản có bị xóa/khóa không
        if (Boolean.TRUE.equals(account.getIsDelete())) {
            log.warn("Tài khoản đã bị xóa: {}", email);
            return "Nếu email tồn tại, bạn sẽ nhận được link reset mật khẩu trong ít phút.";
        }

        // Rate limiting: Kiểm tra số token đã tạo trong 1 giờ qua
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTokenCount = tokenRepository.countRecentTokensByEmail(email, oneHourAgo);
        if (recentTokenCount >= MAX_TOKENS_PER_HOUR) {
            log.warn("Rate limit exceeded cho email: {}", email);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, 
                "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 giờ.");
        }

        // Tạo token mới
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .email(email)
            .token(tokenValue)
            .expiryTime(expiryTime)
            .used(false)
            .build();
        
        tokenRepository.save(resetToken);
        log.info("Đã tạo reset token cho email: {}", email);

        // Build và gửi email
        String resetLink = frontendBaseUrl + "/reset-password?token=" + tokenValue;
        emailService.sendPasswordResetEmail(email, resetLink, TOKEN_EXPIRY_MINUTES);

        return "Nếu email tồn tại, bạn sẽ nhận được link reset mật khẩu trong ít phút.";
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.warn("Token không tồn tại: {}", token.substring(0, Math.min(8, token.length())) + "...");
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        boolean valid = resetToken.isValid();
        
        if (!valid) {
            if (resetToken.isExpired()) {
                log.warn("Token đã hết hạn: {}", token.substring(0, Math.min(8, token.length())) + "...");
            } else if (resetToken.getUsed()) {
                log.warn("Token đã được sử dụng: {}", token.substring(0, Math.min(8, token.length())) + "...");
            }
        }

        return valid;
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String tokenValue = request.getToken();
        String newPassword = request.getNewPassword();

        // Tìm token
        PasswordResetToken resetToken = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Token không hợp lệ"));

        // Validate token
        if (resetToken.getUsed()) {
            log.warn("Token đã được sử dụng trước đó: {}", 
                tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...");
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token đã được sử dụng");
        }

        if (resetToken.isExpired()) {
            log.warn("Token đã hết hạn: {}", 
                tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...");
            throw new ApiException(HttpStatus.BAD_REQUEST, 
                "Token đã hết hạn. Vui lòng yêu cầu reset mật khẩu mới.");
        }

        // Tìm user theo email
        String email = resetToken.getEmail();
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Không tìm thấy account với email: {}", email);
                return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống");
            });

        // Kiểm tra account còn active không
        if (Boolean.TRUE.equals(account.getIsDelete())) {
            log.error("Account đã bị xóa: {}", email);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tài khoản không tồn tại");
        }

        // Encode và cập nhật password mới
        String encodedPassword = passwordEncoder.encode(newPassword);
        account.setPassword(encodedPassword);
        accountRepository.save(account);
        log.info("Đã reset password thành công cho user: {}", account.getUsername());

        // Đánh dấu token đã sử dụng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        log.info("Đã đánh dấu token đã sử dụng");

        // Gửi email thông báo thành công (fire and forget)
        try {
            emailService.sendPasswordResetSuccessEmail(email);
        } catch (Exception e) {
            log.warn("Không gửi được email thông báo thành công: {}", e.getMessage());
        }

        return "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.";
    }
}
