package com.poly.ASM.repository.user;

import com.poly.ASM.entity.user.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository để thao tác với bảng password_reset_tokens.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Tìm token theo giá trị token.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Tìm tất cả token chưa sử dụng và chưa hết hạn của một email.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiryTime > :now")
    List<PasswordResetToken> findValidTokensByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * Đếm số lượng token đã tạo trong khoảng thời gian (dùng cho rate limiting).
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.email = :email AND t.expiryTime > :since")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Xóa tất cả token đã hết hạn hoặc đã sử dụng (cleanup job).
     */
    void deleteByExpiryTimeBeforeOrUsedTrue(LocalDateTime expiryTime);
}
