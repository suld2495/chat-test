package com.chat.chat.websocket.controller;

import com.chat.chat.domain.message.dto.MessageResponse;
import com.chat.chat.domain.message.dto.MessageSendRequest;
import com.chat.chat.domain.message.service.MessageService;
import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.service.UserService;
import com.chat.chat.websocket.dto.ChatMessageDto;
import com.chat.chat.websocket.dto.TypingNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket 채팅 컨트롤러
 * STOMP 메시지 핸들링
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;

    /**
     * 채팅 메시지 전송
     * 클라이언트가 /app/chat/{chatRoomId}로 메시지 전송
     * 브로커가 /topic/chatroom/{chatRoomId}로 구독자들에게 전달
     */
    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable UUID chatRoomId,
            @Payload ChatMessageDto message) {

        log.info("📨 WebSocket message received: chatRoom={}, sender={}, type={}",
                chatRoomId, message.getSenderId(), message.getChatMessageType());

        try {
            // 메시지 타입에 따른 처리
            switch (message.getChatMessageType()) {
                case CHAT -> handleChatMessage(chatRoomId, message);
                case JOIN -> handleJoinMessage(chatRoomId, message);
                case LEAVE -> handleLeaveMessage(chatRoomId, message);
                case READ -> handleReadMessage(chatRoomId, message);
                default -> log.warn("⚠️ Unknown message type: {}", message.getChatMessageType());
            }
        } catch (Exception e) {
            log.error("❌ Error processing WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * 일반 채팅 메시지 처리
     */
    private void handleChatMessage(UUID chatRoomId, ChatMessageDto message) {
        // 데이터베이스에 메시지 저장
        MessageSendRequest request = MessageSendRequest.builder()
                .chatRoomId(chatRoomId)
                .senderId(message.getSenderId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .build();

        MessageResponse savedMessage = messageService.sendMessage(request);

        // 발신자 정보 조회
        User sender = userService.findUserById(message.getSenderId());

        // 저장된 메시지 정보로 WebSocket 메시지 생성
        ChatMessageDto responseMessage = ChatMessageDto.builder()
                .messageId(savedMessage.getId())
                .chatRoomId(chatRoomId)
                .senderId(savedMessage.getSender().getId())
                .senderNickname(sender.getNickname())
                .messageType(savedMessage.getMessageType())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getCreatedAt())
                .chatMessageType(ChatMessageDto.ChatMessageType.CHAT)
                .build();

        // 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                responseMessage
        );

        log.info("✅ Chat message sent to /topic/chatroom/{}", chatRoomId);
    }

    /**
     * 입장 메시지 처리
     */
    private void handleJoinMessage(UUID chatRoomId, ChatMessageDto message) {
        User user = userService.findUserById(message.getSenderId());

        ChatMessageDto joinMessage = ChatMessageDto.builder()
                .chatRoomId(chatRoomId)
                .senderId(message.getSenderId())
                .senderNickname(user.getNickname())
                .content(user.getNickname() + "님이 입장했습니다.")
                .timestamp(LocalDateTime.now())
                .chatMessageType(ChatMessageDto.ChatMessageType.JOIN)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                joinMessage
        );

        log.info("✅ Join message sent: user={}, chatRoom={}", user.getNickname(), chatRoomId);
    }

    /**
     * 퇴장 메시지 처리
     */
    private void handleLeaveMessage(UUID chatRoomId, ChatMessageDto message) {
        User user = userService.findUserById(message.getSenderId());

        ChatMessageDto leaveMessage = ChatMessageDto.builder()
                .chatRoomId(chatRoomId)
                .senderId(message.getSenderId())
                .senderNickname(user.getNickname())
                .content(user.getNickname() + "님이 퇴장했습니다.")
                .timestamp(LocalDateTime.now())
                .chatMessageType(ChatMessageDto.ChatMessageType.LEAVE)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                leaveMessage
        );

        log.info("✅ Leave message sent: user={}, chatRoom={}", user.getNickname(), chatRoomId);
    }

    /**
     * 읽음 확인 처리
     */
    private void handleReadMessage(UUID chatRoomId, ChatMessageDto message) {
        // 모든 메시지 읽음 처리
        messageService.markAllAsRead(chatRoomId, message.getSenderId());

        ChatMessageDto readMessage = ChatMessageDto.builder()
                .chatRoomId(chatRoomId)
                .senderId(message.getSenderId())
                .timestamp(LocalDateTime.now())
                .chatMessageType(ChatMessageDto.ChatMessageType.READ)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                readMessage
        );

        log.info("✅ Read confirmation sent: user={}, chatRoom={}", message.getSenderId(), chatRoomId);
    }

    /**
     * 타이핑 알림
     * 클라이언트가 /app/typing/{chatRoomId}로 타이핑 상태 전송
     */
    @MessageMapping("/typing/{chatRoomId}")
    public void sendTypingNotification(
            @DestinationVariable UUID chatRoomId,
            @Payload TypingNotificationDto notification) {

        log.info("⌨️ Typing notification: chatRoom={}, user={}, isTyping={}",
                chatRoomId, notification.getUserId(), notification.getIsTyping());

        // 채팅방의 다른 사용자들에게 타이핑 알림 전송
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId + "/typing",
                notification
        );
    }
}
