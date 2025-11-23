package com.chat.chat.websocket.dto;

import com.chat.chat.domain.message.entity.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket 채팅 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    /**
     * 메시지 ID
     */
    private UUID messageId;

    /**
     * 채팅방 ID
     */
    private UUID chatRoomId;

    /**
     * 발신자 ID
     */
    private UUID senderId;

    /**
     * 발신자 닉네임
     */
    private String senderNickname;

    /**
     * 메시지 타입
     */
    private MessageType messageType;

    /**
     * 메시지 내용
     */
    private String content;

    /**
     * 전송 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 메시지 이벤트 타입
     */
    private ChatMessageType chatMessageType;

    /**
     * 채팅 메시지 이벤트 타입
     */
    public enum ChatMessageType {
        /**
         * 일반 채팅 메시지
         */
        CHAT,

        /**
         * 사용자 입장
         */
        JOIN,

        /**
         * 사용자 퇴장
         */
        LEAVE,

        /**
         * 읽음 확인
         */
        READ,

        /**
         * 타이핑 중
         */
        TYPING
    }
}
