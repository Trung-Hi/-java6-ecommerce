package com.poly.ASM.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ token để reset mật khẩu.
 * Mỗi token có thời hạn 15 phút và chỉ sử dụng được 1 lần.
 */
@Entity
@Table(
    name = "password_reset_tokens",
    indexes = {
        @Index(name = "idx_token", columnList = "token", unique = true),
        @Index(name = "idx_email", columnList = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Email của user cần reset password.
     * NVARCHAR trong SQL Server -> String trong Java
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Token UUID - unique để tránh collision.
     * NVARCHAR trong SQL Server -> String trong Java
     */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Thời điểm token hết hạn.
     * DATETIME2 trong SQL Server -> LocalDateTime trong Java
     */
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    /**
     * Đánh dấu token đã được sử dụng chưa.
     * BIT trong SQL Server -> Boolean trong Java
     */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    /**
     * Kiểm tra token có hết hạn không.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }

    /**
     * Kiểm tra token có hợp lệ không (chưa dùng và chưa hết hạn).
     */
    public boolean isValid() {
        return !this.used && !isExpired();
    }
}
