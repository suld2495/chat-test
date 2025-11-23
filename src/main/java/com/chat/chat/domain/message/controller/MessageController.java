package com.chat.chat.domain.message.controller;

import com.chat.chat.domain.message.dto.MessageResponse;
import com.chat.chat.domain.message.dto.MessageSendRequest;
import com.chat.chat.domain.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 메시지 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 메시지 전송
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageSendRequest request) {
        log.info("📥 POST /api/messages - Send message: chatRoom={}, sender={}",
                request.getChatRoomId(), request.getSenderId());

        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 채팅방의 메시지 목록 조회 (페이징)
     */
    @GetMapping("/chatroom/{chatRoomId}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable UUID chatRoomId,
            @RequestParam UUID userId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("📥 GET /api/messages/chatroom/{} - Get messages: user={}, page={}, size={}",
                chatRoomId, userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<MessageResponse> response = messageService.getMessages(chatRoomId, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 시간 이후의 메시지 조회
     */
    @GetMapping("/chatroom/{chatRoomId}/since")
    public ResponseEntity<Page<MessageResponse>> getMessagesSince(
            @PathVariable UUID chatRoomId,
            @RequestParam UUID userId,
            @RequestParam String since,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        LocalDateTime sinceTime = LocalDateTime.parse(since);
        log.info("📥 GET /api/messages/chatroom/{}/since - Get messages since: {}", chatRoomId, sinceTime);

        Page<MessageResponse> response = messageService.getMessagesSince(chatRoomId, userId, sinceTime, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 읽지 않은 메시지 조회
     */
    @GetMapping("/chatroom/{chatRoomId}/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages(
            @PathVariable UUID chatRoomId,
            @RequestParam UUID userId) {

        log.info("📥 GET /api/messages/chatroom/{}/unread - Get unread messages: user={}", chatRoomId, userId);

        List<MessageResponse> response = messageService.getUnreadMessages(chatRoomId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @GetMapping("/chatroom/{chatRoomId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(
            @PathVariable UUID chatRoomId,
            @RequestParam UUID userId) {

        log.info("📥 GET /api/messages/chatroom/{}/unread-count - Get unread count: user={}", chatRoomId, userId);

        Long count = messageService.getUnreadMessageCount(chatRoomId, userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * 메시지 읽음 처리
     */
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable UUID messageId,
            @RequestBody Map<String, UUID> request) {

        UUID userId = request.get("userId");
        log.info("📥 PATCH /api/messages/{}/read - Mark as read: user={}", messageId, userId);

        messageService.markMessageAsRead(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 채팅방의 모든 메시지 읽음 처리
     */
    @PatchMapping("/chatroom/{chatRoomId}/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @PathVariable UUID chatRoomId,
            @RequestBody Map<String, UUID> request) {

        UUID userId = request.get("userId");
        log.info("📥 PATCH /api/messages/chatroom/{}/read-all - Mark all as read: user={}", chatRoomId, userId);

        int count = messageService.markAllAsRead(chatRoomId, userId);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    /**
     * 메시지 삭제
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {

        log.info("📥 DELETE /api/messages/{} - Delete message: user={}", messageId, userId);

        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
