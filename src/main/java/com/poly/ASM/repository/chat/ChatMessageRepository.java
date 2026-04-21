package com.poly.ASM.repository.chat;

import com.poly.ASM.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByCustomerIdAndProductIdOrderByCreatedAtAsc(String customerId, Integer productId);

    List<ChatMessage> findTop500ByOrderByCreatedAtDesc();

    @Query(value = "SELECT TOP 1 admin_id FROM dbo.chat_messages WHERE customer_id = :customerId AND product_id = :productId AND admin_id IS NOT NULL ORDER BY created_at DESC", nativeQuery = true)
    String findAssignedAdminId(@Param("customerId") String customerId, @Param("productId") Integer productId);

    @Modifying
    @Query(value = "UPDATE dbo.chat_messages SET admin_id = :adminId WHERE customer_id = :customerId AND product_id = :productId AND admin_id IS NULL", nativeQuery = true)
    int assignAdminIfNull(@Param("adminId") String adminId, @Param("customerId") String customerId, @Param("productId") Integer productId);
}
