package com.chat.chat.domain.user.repository;

import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 활성화된 사용자 조회
     */
    List<User> findByIsActiveTrue();

    /**
     * 특정 상태의 사용자 조회
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     */
    boolean existsByNickname(String nickname);

    /**
     * 닉네임으로 사용자 검색 (LIKE)
     */
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:keyword% AND u.isActive = true")
    List<User> searchByNickname(@Param("keyword") String keyword);
}
