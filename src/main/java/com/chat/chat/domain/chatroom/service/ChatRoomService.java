package com.chat.chat.domain.chatroom.service;

import com.chat.chat.common.exception.EntityNotFoundException;
import com.chat.chat.common.exception.ErrorCode;
import com.chat.chat.common.exception.InvalidValueException;
import com.chat.chat.domain.chatroom.dto.ChatRoomResponse;
import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.chatroom.repository.ChatRoomRepository;
import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 채팅방 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     */
    @Transactional
    public ChatRoomResponse createOrGetChatRoom(UUID user1Id, UUID user2Id) {
        // 자기 자신과의 채팅방 생성 방지
        if (user1Id.equals(user2Id)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE,
                    "자기 자신과는 채팅할 수 없습니다.");
        }

        User user1 = userService.findUserById(user1Id);
        User user2 = userService.findUserById(user2Id);

        // 기존 채팅방 존재 여부 확인
        return chatRoomRepository.findByUsers(user1, user2)
                .map(chatRoom -> {
                    log.info("✅ Existing chat room found: {}", chatRoom.getId());
                    return ChatRoomResponse.from(chatRoom);
                })
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.create(user1, user2);
                    ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
                    log.info("✅ New chat room created: {}", savedChatRoom.getId());
                    return ChatRoomResponse.from(savedChatRoom);
                });
    }

    /**
     * 채팅방 ID로 조회
     */
    public ChatRoomResponse getChatRoomById(UUID chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND,
                        "채팅방을 찾을 수 없습니다: " + chatRoomId));

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 사용자의 모든 채팅방 조회 (최신순)
     */
    public List<ChatRoomResponse> getUserChatRooms(UUID userId) {
        return chatRoomRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(chatRoom -> ChatRoomResponse.fromForUser(chatRoom, userId))
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 메시지가 있는 채팅방 목록
     */
    public List<ChatRoomResponse> getChatRoomsWithUnreadMessages(UUID userId) {
        return chatRoomRepository.findChatRoomsWithUnreadMessages(userId)
                .stream()
                .map(chatRoom -> ChatRoomResponse.fromForUser(chatRoom, userId))
                .collect(Collectors.toList());
    }

    /**
     * 전체 읽지 않은 메시지 수 조회
     */
    public Long getTotalUnreadCount(UUID userId) {
        return chatRoomRepository.getTotalUnreadCount(userId);
    }

    /**
     * 읽지 않은 메시지 수 초기화
     */
    @Transactional
    public void resetUnreadCount(UUID chatRoomId, UUID userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.isParticipant(userId)) {
            throw new InvalidValueException(ErrorCode.NOT_CHAT_ROOM_PARTICIPANT,
                    "채팅방 참여자가 아닙니다.");
        }

        chatRoom.resetUnreadCount(userId);
        log.info("✅ Unread count reset: chatRoom={}, user={}", chatRoomId, userId);
    }

    /**
     * 마지막 메시지 업데이트
     */
    @Transactional
    public void updateLastMessage(UUID chatRoomId, String message, UUID senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoom.updateLastMessage(message, senderId);
        log.info("✅ Last message updated: chatRoom={}", chatRoomId);
    }

    /**
     * 채팅방 Entity 조회 (내부 사용)
     */
    public ChatRoom findChatRoomById(UUID chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND,
                        "채팅방을 찾을 수 없습니다: " + chatRoomId));
    }

    /**
     * 채팅방 참여자 확인
     */
    public void validateParticipant(UUID chatRoomId, UUID userId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        if (!chatRoom.isParticipant(userId)) {
            throw new InvalidValueException(ErrorCode.NOT_CHAT_ROOM_PARTICIPANT,
                    "채팅방 참여자가 아닙니다.");
        }
    }
}
