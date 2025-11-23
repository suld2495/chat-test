package com.chat.chat.domain.chatroom.controller;

import com.chat.chat.domain.chatroom.dto.ChatRoomResponse;
import com.chat.chat.domain.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 채팅방 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createOrGetChatRoom(@RequestBody Map<String, UUID> request) {
        UUID user1Id = request.get("user1Id");
        UUID user2Id = request.get("user2Id");

        log.info("📥 POST /api/chatrooms - Create or get chat room: user1={}, user2={}", user1Id, user2Id);

        ChatRoomResponse response = chatRoomService.createOrGetChatRoom(user1Id, user2Id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 채팅방 ID로 조회
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoomById(@PathVariable UUID chatRoomId) {
        log.info("📥 GET /api/chatrooms/{} - Get chat room by ID", chatRoomId);
        ChatRoomResponse response = chatRoomService.getChatRoomById(chatRoomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 모든 채팅방 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(@PathVariable UUID userId) {
        log.info("📥 GET /api/chatrooms/user/{} - Get user's chat rooms", userId);
        List<ChatRoomResponse> response = chatRoomService.getUserChatRooms(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 읽지 않은 메시지가 있는 채팅방 목록
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsWithUnreadMessages(@PathVariable UUID userId) {
        log.info("📥 GET /api/chatrooms/user/{}/unread - Get chat rooms with unread messages", userId);
        List<ChatRoomResponse> response = chatRoomService.getChatRoomsWithUnreadMessages(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 읽지 않은 메시지 수 조회
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getTotalUnreadCount(@PathVariable UUID userId) {
        log.info("📥 GET /api/chatrooms/user/{}/unread-count - Get total unread count", userId);
        Long count = chatRoomService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(Map.of("totalUnreadCount", count));
    }

    /**
     * 읽지 않은 메시지 수 초기화
     */
    @PatchMapping("/{chatRoomId}/read")
    public ResponseEntity<Void> resetUnreadCount(
            @PathVariable UUID chatRoomId,
            @RequestBody Map<String, UUID> request) {

        UUID userId = request.get("userId");
        log.info("📥 PATCH /api/chatrooms/{}/read - Reset unread count: user={}", chatRoomId, userId);

        chatRoomService.resetUnreadCount(chatRoomId, userId);
        return ResponseEntity.noContent().build();
    }
}
