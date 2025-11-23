package com.chat.chat.domain.message.repository;

import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.message.entity.Message;
import com.chat.chat.domain.message.entity.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 메시지 리포지토리
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * 채팅방의 메시지 조회 (페이징, 최신순)
     */
    Page<Message> findByChatRoomAndIsDeletedFalseOrderByCreatedAtDesc(
            ChatRoom chatRoom, Pageable pageable);

    /**
     * 채팅방의 메시지 조회 (특정 시간 이후, 페이징)
     */
    Page<Message> findByChatRoomAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(
            ChatRoom chatRoom, LocalDateTime after, Pageable pageable);

    /**
     * 채팅방의 읽지 않은 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE " +
            "m.chatRoom = :chatRoom AND " +
            "m.sender.id <> :userId AND " +
            "m.isRead = false AND " +
            "m.isDeleted = false " +
            "ORDER BY m.createdAt ASC")
    List<Message> findUnreadMessages(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("userId") UUID userId);

    /**
     * 채팅방의 읽지 않은 메시지 수 조회
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.chatRoom = :chatRoom AND " +
            "m.sender.id <> :userId AND " +
            "m.isRead = false AND " +
            "m.isDeleted = false")
    Long countUnreadMessages(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("userId") UUID userId);

    /**
     * 채팅방의 마지막 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE " +
            "m.chatRoom = :chatRoom AND " +
            "m.isDeleted = false " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("chatRoom") ChatRoom chatRoom);

    /**
     * 채팅방의 모든 읽지 않은 메시지를 읽음 처리
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE " +
            "m.chatRoom = :chatRoom AND " +
            "m.sender.id <> :userId AND " +
            "m.isRead = false")
    int markAllAsRead(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt);

    /**
     * 특정 타입의 메시지 조회
     */
    List<Message> findByChatRoomAndMessageTypeAndIsDeletedFalse(
            ChatRoom chatRoom, MessageType messageType);

    /**
     * 채팅방의 전체 메시지 수 조회
     */
    Long countByChatRoomAndIsDeletedFalse(ChatRoom chatRoom);
}
