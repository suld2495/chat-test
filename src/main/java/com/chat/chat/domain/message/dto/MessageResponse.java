package com.chat.chat.domain.message.dto;

import com.chat.chat.domain.message.entity.Message;
import com.chat.chat.domain.message.entity.MessageType;
import com.chat.chat.domain.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
public class MessageResponse {

    private UUID id;
    private UUID chatRoomId;
    private UserResponse sender;
    private MessageType messageType;
    private String content;
    private Boolean isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Entity to DTO
     */
    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .sender(UserResponse.from(message.getSender()))
                .messageType(message.getMessageType())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
