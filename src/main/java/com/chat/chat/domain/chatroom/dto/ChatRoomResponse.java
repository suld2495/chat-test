package com.chat.chat.domain.chatroom.dto;

import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 채팅방 응답 DTO
 */
@Getter
@Builder
public class ChatRoomResponse {

    private UUID id;
    private UserResponse user1;
    private UserResponse user2;
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageAt;

    private Integer user1UnreadCount;
    private Integer user2UnreadCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Entity to DTO
     */
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .user1(UserResponse.from(chatRoom.getUser1()))
                .user2(UserResponse.from(chatRoom.getUser2()))
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .user1UnreadCount(chatRoom.getUser1UnreadCount())
                .user2UnreadCount(chatRoom.getUser2UnreadCount())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }

    /**
     * 특정 사용자 관점의 채팅방 정보
     */
    public static ChatRoomResponse fromForUser(ChatRoom chatRoom, UUID currentUserId) {
        UserResponse otherUser = currentUserId.equals(chatRoom.getUser1().getId())
                ? UserResponse.from(chatRoom.getUser2())
                : UserResponse.from(chatRoom.getUser1());

        Integer unreadCount = currentUserId.equals(chatRoom.getUser1().getId())
                ? chatRoom.getUser1UnreadCount()
                : chatRoom.getUser2UnreadCount();

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .user1(otherUser) // 상대방만 표시
                .user2(null)
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .user1UnreadCount(unreadCount)
                .user2UnreadCount(null)
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}
