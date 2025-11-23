package com.chat.chat.domain.chatroom.entity;

import com.chat.chat.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅방 엔티티
 * 1:1 채팅방을 관리합니다.
 */
@Entity
@Table(name = "chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_users",
                        columnNames = {"user1_id", "user2_id"}
                )
        },
        indexes = {
                @Index(name = "idx_chat_room_user1", columnList = "user1_id"),
                @Index(name = "idx_chat_room_user2", columnList = "user2_id"),
                @Index(name = "idx_chat_room_updated", columnList = "updated_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

    /**
     * 채팅방 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_room_id", columnDefinition = "UUID")
    private UUID id;

    /**
     * 첫 번째 사용자
     * user1_id < user2_id 규칙으로 항상 작은 ID가 user1
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    /**
     * 두 번째 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    /**
     * 마지막 메시지 내용
     */
    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    /**
     * 마지막 메시지 시간
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * user1의 읽지 않은 메시지 수
     */
    @Column(name = "user1_unread_count", nullable = false)
    @Builder.Default
    private Integer user1UnreadCount = 0;

    /**
     * user2의 읽지 않은 메시지 수
     */
    @Column(name = "user2_unread_count", nullable = false)
    @Builder.Default
    private Integer user2UnreadCount = 0;

    /**
     * 채팅방 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 채팅방 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 채팅방 활성화 여부
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 마지막 메시지 업데이트
     */
    public void updateLastMessage(String message, UUID senderId) {
        this.lastMessage = message;
        this.lastMessageAt = LocalDateTime.now();

        // 발신자가 아닌 사용자의 읽지 않은 메시지 수 증가
        if (senderId.equals(user1.getId())) {
            this.user2UnreadCount++;
        } else {
            this.user1UnreadCount++;
        }
    }

    /**
     * 읽지 않은 메시지 수 초기화
     */
    public void resetUnreadCount(UUID userId) {
        if (userId.equals(user1.getId())) {
            this.user1UnreadCount = 0;
        } else if (userId.equals(user2.getId())) {
            this.user2UnreadCount = 0;
        }
    }

    /**
     * 특정 사용자가 이 채팅방의 참여자인지 확인
     */
    public boolean isParticipant(UUID userId) {
        return userId.equals(user1.getId()) || userId.equals(user2.getId());
    }

    /**
     * 상대방 사용자 가져오기
     */
    public User getOtherUser(UUID userId) {
        if (userId.equals(user1.getId())) {
            return user2;
        } else if (userId.equals(user2.getId())) {
            return user1;
        }
        return null;
    }

    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 두 사용자로 채팅방 생성 (정렬된 순서로)
     */
    public static ChatRoom create(User user1, User user2) {
        // UUID를 비교하여 항상 작은 값이 user1이 되도록 정렬
        User sortedUser1 = user1;
        User sortedUser2 = user2;

        if (user1.getId().compareTo(user2.getId()) > 0) {
            sortedUser1 = user2;
            sortedUser2 = user1;
        }

        return ChatRoom.builder()
                .user1(sortedUser1)
                .user2(sortedUser2)
                .build();
    }
}
