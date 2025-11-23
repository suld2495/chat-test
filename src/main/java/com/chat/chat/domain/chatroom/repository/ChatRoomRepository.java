package com.chat.chat.domain.chatroom.repository;

import com.chat.chat.domain.chatroom.entity.ChatRoom;
import com.chat.chat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 채팅방 리포지토리
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    /**
     * 두 사용자 간의 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.user1 = :user1 AND cr.user2 = :user2) OR " +
            "(cr.user1 = :user2 AND cr.user2 = :user1)")
    Optional<ChatRoom> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 사용자가 참여한 모든 채팅방 조회 (최신순)
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.user1.id = :userId OR cr.user2.id = :userId) " +
            "AND cr.isActive = true " +
            "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId);

    /**
     * 특정 사용자의 읽지 않은 메시지가 있는 채팅방 목록 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "((cr.user1.id = :userId AND cr.user1UnreadCount > 0) OR " +
            "(cr.user2.id = :userId AND cr.user2UnreadCount > 0)) " +
            "AND cr.isActive = true " +
            "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findChatRoomsWithUnreadMessages(@Param("userId") UUID userId);

    /**
     * 특정 사용자의 전체 읽지 않은 메시지 수 조회
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN cr.user1.id = :userId THEN cr.user1UnreadCount " +
            "WHEN cr.user2.id = :userId THEN cr.user2UnreadCount ELSE 0 END), 0) " +
            "FROM ChatRoom cr WHERE " +
            "(cr.user1.id = :userId OR cr.user2.id = :userId) " +
            "AND cr.isActive = true")
    Long getTotalUnreadCount(@Param("userId") UUID userId);

    /**
     * 활성화된 채팅방만 조회
     */
    List<ChatRoom> findByIsActiveTrueOrderByUpdatedAtDesc();
}
