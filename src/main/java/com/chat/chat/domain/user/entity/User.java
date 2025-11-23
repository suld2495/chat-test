package com.chat.chat.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 엔티티
 * Supabase Auth와 연동되어 사용자 정보를 관리합니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    /**
     * 사용자 ID (UUID)
     * Supabase Auth의 user.id와 동일하게 관리
     */
    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID id;

    /**
     * 사용자 이메일
     */
    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * 사용자 닉네임
     */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * 사용자 상태 (ONLINE, OFFLINE, AWAY)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    /**
     * 마지막 접속 시간
     */
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    /**
     * 계정 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 계정 활성화 여부
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 사용자 상태 변경
     */
    public void updateStatus(UserStatus status) {
        this.status = status;
        if (status == UserStatus.ONLINE || status == UserStatus.AWAY) {
            this.lastSeenAt = LocalDateTime.now();
        }
    }

    /**
     * 프로필 정보 업데이트
     */
    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.status = UserStatus.OFFLINE;
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}
