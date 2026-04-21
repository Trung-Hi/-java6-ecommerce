package com.poly.ASM.service.email;

/**
 * Service interface để gửi email.
 */
public interface EmailService {

    /**
     * Gửi email reset password với link chứa token.
     *
     * @param toEmail    email người nhận
     * @param resetLink  link reset password (bao gồm token)
     * @param expiryMinutes thời gian hết hạn của token (phút)
     */
    void sendPasswordResetEmail(String toEmail, String resetLink, int expiryMinutes);

    /**
     * Gửi email thông báo password đã được reset thành công.
     *
     * @param toEmail email người nhận
     */
    void sendPasswordResetSuccessEmail(String toEmail);

    /**
     * Gửi email chứa mật khẩu tạm thời sau khi admin reset password.
     *
     * @param toEmail       email người nhận
     * @param tempPassword  mật khẩu tạm thời
     * @param username      tên đăng nhập
     */
    void sendTemporaryPasswordEmail(String toEmail, String tempPassword, String username);
}
