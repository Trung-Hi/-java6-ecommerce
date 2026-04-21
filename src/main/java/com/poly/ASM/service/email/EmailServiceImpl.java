package com.poly.ASM.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementation của EmailService sử dụng JavaMailSender.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:system@example.com}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "Hệ thống ASM");
            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu reset mật khẩu");

            String htmlContent = buildResetPasswordEmail(resetLink, expiryMinutes);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email reset password đến: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Lỗi gửi email reset password đến {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi email: {}", e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi gửi email.");
        }
    }

    @Override
    public void sendPasswordResetSuccessEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "Hệ thống ASM");
            helper.setTo(toEmail);
            helper.setSubject("Mật khẩu đã được thay đổi thành công");

            String htmlContent = buildSuccessEmail();
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email thông báo reset password thành công đến: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi gửi email thông báo thành công: {}", e.getMessage());
            // Không throw exception vì đây chỉ là email thông báo, không ảnh hưởng flow chính
        }
    }

    /**
     * Build HTML content cho email reset password.
     */
    private String buildResetPasswordEmail(String resetLink, int expiryMinutes) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #3b82f6; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #3b82f6; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .button:hover { background: #2563eb; }
                    .link-box { background: #e5e7eb; padding: 10px; border-radius: 4px; word-break: break-all; font-size: 12px; }
                    .warning { color: #dc2626; font-size: 14px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>Reset Mật khẩu</h2>
                    </div>
                    <div class="content">
                        <p>Xin chào,</p>
                        <p>Bạn đã yêu cầu reset mật khẩu. Vui lòng click vào nút bên dưới để tiếp tục:</p>
                        
                        <center>
                            <a href="%s" class="button">Reset Mật khẩu</a>
                        </center>
                        
                        <p>Hoặc copy link này vào trình duyệt:</p>
                        <div class="link-box">%s</div>
                        
                        <p class="warning">⚠️ Link này sẽ hết hạn sau %d phút và chỉ sử dụng được 1 lần.</p>
                        
                        <p style="margin-top: 30px; font-size: 12px; color: #6b7280;">
                            Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink, expiryMinutes);
    }

    @Override
    public void sendTemporaryPasswordEmail(String toEmail, String tempPassword, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "Hệ thống ASM");
            helper.setTo(toEmail);
            helper.setSubject("Mật khẩu của bạn đã được đặt lại");

            String htmlContent = buildTemporaryPasswordEmail(tempPassword, username);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email mật khẩu tạm đến: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Lỗi gửi email mật khẩu tạm đến {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi email: {}", e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi gửi email.");
        }
    }

    /**
     * Build HTML content cho email gửi mật khẩu tạm.
     */
    private String buildTemporaryPasswordEmail(String tempPassword, String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f59e0b; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fffbeb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .password-box { background: #fef3c7; padding: 15px; border-radius: 8px; text-align: center; 
                                    font-size: 24px; font-weight: bold; letter-spacing: 2px; margin: 20px 0;
                                    border: 2px dashed #f59e0b; color: #92400e; }
                    .warning { color: #dc2626; font-size: 14px; margin-top: 20px; }
                    .info { background: #e0f2fe; padding: 15px; border-radius: 6px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🔐 Mật khẩu đã được đặt lại</h2>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Admin đã đặt lại mật khẩu cho tài khoản của bạn. Dưới đây là mật khẩu tạm thời:</p>
                        
                        <div class="password-box">%s</div>
                        
                        <div class="info">
                            <strong>Thông tin đăng nhập:</strong><br>
                            Username: %s<br>
                            Mật khẩu: %s
                        </div>
                        
                        <p class="warning">⚠️ Vui lòng đăng nhập và đổi mật khẩu ngay để bảo mật tài khoản!</p>
                        
                        <p style="margin-top: 30px; font-size: 12px; color: #6b7280;">
                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng liên hệ admin ngay.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, tempPassword, username, tempPassword);
    }

    /**
     * Build HTML content cho email thông báo thành công.
     */
    private String buildSuccessEmail() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #22c55e; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f0fdf4; padding: 30px; border-radius: 0 0 8px 8px; }
                    .success-icon { font-size: 48px; text-align: center; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>✓ Thành công!</h2>
                    </div>
                    <div class="content">
                        <div class="success-icon">🔒</div>
                        <p>Xin chào,</p>
                        <p>Mật khẩu của bạn đã được thay đổi thành công.</p>
                        <p>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ ngay với bộ phận hỗ trợ.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
