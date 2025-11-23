package com.chat.chat.domain.message.service;

import com.chat.chat.common.exception.EntityNotFoundException;
import com.chat.chat.common.exception.ErrorCode;
import com.chat.chat.common.exception.InvalidValueException;
import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.chatroom.service.ChatRoomService;
import com.chat.chat.domain.message.dto.MessageResponse;
import com.chat.chat.domain.message.dto.MessageSendRequest;
import com.chat.chat.domain.message.entity.Message;
import com.chat.chat.domain.message.repository.MessageRepository;
import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 메시지 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;

    /**
     * 메시지 전송
     */
    @Transactional
    public MessageResponse sendMessage(MessageSendRequest request) {
        // 채팅방 존재 여부 및 참여자 확인
        ChatRoom chatRoom = chatRoomService.findChatRoomById(request.getChatRoomId());
        chatRoomService.validateParticipant(request.getChatRoomId(), request.getSenderId());

        // 발신자 조회
        User sender = userService.findUserById(request.getSenderId());

        // 메시지 생성
        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(request.getMessageType())
                .content(request.getContent())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 채팅방의 마지막 메시지 업데이트
        chatRoomService.updateLastMessage(
                request.getChatRoomId(),
                request.getContent(),
                request.getSenderId()
        );

        log.info("✅ Message sent: {} in chatRoom {}", savedMessage.getId(), request.getChatRoomId());

        return MessageResponse.from(savedMessage);
    }

    /**
     * 채팅방의 메시지 목록 조회 (페이징)
     */
    public Page<MessageResponse> getMessages(UUID chatRoomId, UUID userId, Pageable pageable) {
        // 채팅방 참여자 확인
        chatRoomService.validateParticipant(chatRoomId, userId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        return messageRepository.findByChatRoomAndIsDeletedFalseOrderByCreatedAtDesc(chatRoom, pageable)
                .map(MessageResponse::from);
    }

    /**
     * 특정 시간 이후의 메시지 조회
     */
    public Page<MessageResponse> getMessagesSince(UUID chatRoomId, UUID userId,
                                                   LocalDateTime since, Pageable pageable) {
        chatRoomService.validateParticipant(chatRoomId, userId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        return messageRepository.findByChatRoomAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(
                chatRoom, since, pageable
        ).map(MessageResponse::from);
    }

    /**
     * 읽지 않은 메시지 조회
     */
    public List<MessageResponse> getUnreadMessages(UUID chatRoomId, UUID userId) {
        chatRoomService.validateParticipant(chatRoomId, userId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        return messageRepository.findUnreadMessages(chatRoom, userId)
                .stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessageAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MESSAGE_NOT_FOUND,
                        "메시지를 찾을 수 없습니다: " + messageId));

        // 발신자는 자신의 메시지를 읽음 처리할 수 없음
        if (message.isSentBy(userId)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE,
                    "자신이 보낸 메시지는 읽음 처리할 수 없습니다.");
        }

        message.markAsRead();
        log.info("✅ Message marked as read: {}", messageId);
    }

    /**
     * 채팅방의 모든 읽지 않은 메시지 읽음 처리
     */
    @Transactional
    public int markAllAsRead(UUID chatRoomId, UUID userId) {
        chatRoomService.validateParticipant(chatRoomId, userId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        int count = messageRepository.markAllAsRead(chatRoom, userId, LocalDateTime.now());

        // 채팅방의 읽지 않은 메시지 카운트 초기화
        chatRoomService.resetUnreadCount(chatRoomId, userId);

        log.info("✅ {} messages marked as read in chatRoom {}", count, chatRoomId);

        return count;
    }

    /**
     * 메시지 삭제
     */
    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MESSAGE_NOT_FOUND));

        // 본인이 보낸 메시지만 삭제 가능
        if (!message.isSentBy(userId)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE,
                    "자신이 보낸 메시지만 삭제할 수 있습니다.");
        }

        message.delete();
        log.info("✅ Message deleted: {}", messageId);
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    public Long getUnreadMessageCount(UUID chatRoomId, UUID userId) {
        chatRoomService.validateParticipant(chatRoomId, userId);

        ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

        return messageRepository.countUnreadMessages(chatRoom, userId);
    }
}
