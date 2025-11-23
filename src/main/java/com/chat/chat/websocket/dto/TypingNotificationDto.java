package com.chat.chat.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 타이핑 알림 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingNotificationDto {

    /**
     * 채팅방 ID
     */
    private UUID chatRoomId;

    /**
     * 타이핑 중인 사용자 ID
     */
    private UUID userId;

    /**
     * 타이핑 중인 사용자 닉네임
     */
    private String nickname;

    /**
     * 타이핑 상태 (true: 타이핑 중, false: 타이핑 종료)
     */
    private Boolean isTyping;
}
