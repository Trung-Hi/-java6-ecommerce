package com.poly.ASM.service.auth;

import com.poly.ASM.dto.request.ForgotPasswordRequest;
import com.poly.ASM.dto.request.ResetPasswordRequest;

/**
 * Service xử lý flow forgot/reset password.
 */
public interface PasswordResetService {

    /**
     * Gửi email reset password.
     * Tạo token mới và gửi link qua email.
     *
     * @param request chứa email cần reset
     * @return message thông báo kết quả
     */
    String sendResetPasswordEmail(ForgotPasswordRequest request);

    /**
     * Xác thực token có hợp lệ không.
     *
     * @param token token cần verify
     * @return true nếu token hợp lệ (chưa dùng, chưa hết hạn)
     */
    boolean verifyToken(String token);

    /**
     * Reset mật khẩu với token hợp lệ.
     *
     * @param request chứa token và mật khẩu mới
     * @return message thông báo kết quả
     */
    String resetPassword(ResetPasswordRequest request);
}
