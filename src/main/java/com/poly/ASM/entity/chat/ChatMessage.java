package com.poly.ASM.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "admin_id", length = 50)
    private String adminId;

    @Lob
    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "sender_role", nullable = false, length = 10)
    private String senderRole;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (senderRole == null || senderRole.isBlank()) {
            senderRole = "USER";
        }
    }
}

