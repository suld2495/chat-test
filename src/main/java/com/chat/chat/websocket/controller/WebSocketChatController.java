package com.chat.chat.websocket.controller;

import com.chat.chat.common.ai.ClaudeChatService;
import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.chatroom.service.ChatRoomService;
import com.chat.chat.domain.message.dto.MessageResponse;
import com.chat.chat.domain.message.dto.MessageSendRequest;
import com.chat.chat.domain.message.entity.MessageType;
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
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ClaudeChatService claudeChatService;

    /**
     * 채팅 메시지 전송
     * 클라이언트가 /app/chat/{chatRoomId}로 메시지 전송
     * 브로커가 /topic/chatroom/{chatRoomId}로 구독자들에게 전달
     */
    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable UUID chatRoomId,
            @Payload ChatMessageDto message) {

        ChatMessageDto.ChatMessageType chatMessageType = message.getChatMessageType() != null
                ? message.getChatMessageType()
                : ChatMessageDto.ChatMessageType.CHAT; // 기본값 방어

        log.info("WebSocket message received: chatRoom={}, sender={}, type={}",
                chatRoomId, message.getSenderId(), chatMessageType);

        try {
            switch (chatMessageType) {
                case CHAT -> handleChatMessage(chatRoomId, message);
                case JOIN -> handleJoinMessage(chatRoomId, message);
                case LEAVE -> handleLeaveMessage(chatRoomId, message);
                case READ -> handleReadMessage(chatRoomId, message);
                default -> log.warn("Unknown message type: {}", chatMessageType);
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * 일반 채팅 메시지 처리
     */
    private void handleChatMessage(UUID chatRoomId, ChatMessageDto message) {
        MessageType messageType = message.getMessageType() != null
                ? message.getMessageType()
                : MessageType.TEXT; // WebSocket 클라이언트가 비워도 TEXT로 저장

        MessageSendRequest request = MessageSendRequest.builder()
                .chatRoomId(chatRoomId)
                .senderId(message.getSenderId())
                .content(message.getContent())
                .messageType(messageType)
                .build();

        MessageResponse savedMessage = messageService.sendMessage(request);
        User sender = userService.findUserById(message.getSenderId());

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

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                responseMessage
        );

        log.info("Chat message sent to /topic/chatroom/{}", chatRoomId);

        triggerBotResponse(chatRoomId, message, sender);
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

        log.info("Join message sent: user={}, chatRoom={}", user.getNickname(), chatRoomId);
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

        log.info("Leave message sent: user={}, chatRoom={}", user.getNickname(), chatRoomId);
    }

    /**
     * 읽음 확인 처리
     */
    private void handleReadMessage(UUID chatRoomId, ChatMessageDto message) {
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

        log.info("Read confirmation sent: user={}, chatRoom={}", message.getSenderId(), chatRoomId);
    }

    /**
     * 타이핑 알림
     * 클라이언트가 /app/typing/{chatRoomId}로 타이핑 상태 전송
     */
    @MessageMapping("/typing/{chatRoomId}")
    public void sendTypingNotification(
            @DestinationVariable UUID chatRoomId,
            @Payload TypingNotificationDto notification) {

        log.info("Typing notification: chatRoom={}, user={}, isTyping={}",
                chatRoomId, notification.getUserId(), notification.getIsTyping());

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId + "/typing",
                notification
        );
    }

    /**
     * 사용자 메시지에 대한 챗봇 응답 트리거
     */
    private void triggerBotResponse(UUID chatRoomId, ChatMessageDto userMessage, User sender) {
        if (isBotUser(sender)) {
            return;
        }

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);
        User botUser = resolveBotUser(chatRoom);
        if (botUser == null) {
            log.warn("[BOT] 챗봇 사용자를 찾을 수 없습니다: chatRoomId={}", chatRoomId);
            return;
        }

        ClaudeChatService.ReplyResult reply = claudeChatService.requestReply(chatRoomId, userMessage.getContent());

        if (reply.isHasReply()) {
            sendBotMessage(chatRoomId, botUser, reply.getReplyText(), MessageType.TEXT);
        }

        if (reply.isLimitReached() && reply.isLimitJustReached()) {
            sendBotMessage(
                    chatRoomId,
                    botUser,
                    "이 채팅방의 챗봇 토큰 한도(" + claudeChatService.getTokenLimitPerRoom() + "토큰)를 모두 사용했습니다. 새 채팅방을 생성해 주세요.",
                    MessageType.SYSTEM
            );
        }
    }

    /**
     * 챗봇 메시지 전송 및 브로드캐스트
     */
    private void sendBotMessage(UUID chatRoomId, User botUser, String content, MessageType messageType) {
        if (content == null || content.isBlank()) {
            return;
        }

        MessageSendRequest request = MessageSendRequest.builder()
                .chatRoomId(chatRoomId)
                .senderId(botUser.getId())
                .content(content)
                .messageType(messageType)
                .build();

        MessageResponse savedMessage = messageService.sendMessage(request);

        ChatMessageDto responseMessage = ChatMessageDto.builder()
                .messageId(savedMessage.getId())
                .chatRoomId(chatRoomId)
                .senderId(savedMessage.getSender().getId())
                .senderNickname(botUser.getNickname())
                .messageType(savedMessage.getMessageType())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getCreatedAt())
                .chatMessageType(ChatMessageDto.ChatMessageType.CHAT)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatRoomId,
                responseMessage
        );

        log.info("[BOT] Reply sent to chatRoom {} (type={}): {}", chatRoomId, messageType, content);
    }

    private User resolveBotUser(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }
        // Lazy proxy를 안전하게 처리하기 위해 ID만 꺼내고 실제 엔티티를 조회
        UUID user1Id = chatRoom.getUser1() != null ? chatRoom.getUser1().getId() : null;
        UUID user2Id = chatRoom.getUser2() != null ? chatRoom.getUser2().getId() : null;

        if (user1Id != null) {
            User user1 = userService.findUserById(user1Id);
            if (isBotUser(user1)) {
                return user1;
            }
        }
        if (user2Id != null) {
            User user2 = userService.findUserById(user2Id);
            if (isBotUser(user2)) {
                return user2;
            }
        }
        return null;
    }

    private boolean isBotUser(User user) {
        return user != null && user.getEmail() != null && user.getEmail().startsWith("bot-");
    }
}
