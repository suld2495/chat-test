package com.chat.chat.domain.message.entity;

import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 메시지 엔티티
 * 채팅방의 메시지를 관리합니다.
 */
@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_message_chat_room", columnList = "chat_room_id"),
                @Index(name = "idx_message_sender", columnList = "sender_id"),
                @Index(name = "idx_message_created", columnList = "created_at"),
                @Index(name = "idx_message_chat_room_created", columnList = "chat_room_id, created_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message {

    /**
     * 메시지 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", columnDefinition = "UUID")
    private UUID id;

    /**
     * 채팅방
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 발신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * 메시지 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * 메시지 내용
     */
    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 읽음 여부
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 읽은 시간
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 메시지 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 삭제 여부
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 삭제 시간
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 메시지를 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 메시지 삭제
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.content = "삭제된 메시지입니다.";
    }

    /**
     * 메시지가 발신자의 것인지 확인
     */
    public boolean isSentBy(UUID userId) {
        return this.sender.getId().equals(userId);
    }
}
