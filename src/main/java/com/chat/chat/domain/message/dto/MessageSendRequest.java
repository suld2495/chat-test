package com.chat.chat.domain.message.dto;

import com.chat.chat.domain.message.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 메시지 전송 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private UUID chatRoomId;

    @NotNull(message = "발신자 ID는 필수입니다.")
    private UUID senderId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 5000, message = "메시지는 5000자를 초과할 수 없습니다.")
    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
}
