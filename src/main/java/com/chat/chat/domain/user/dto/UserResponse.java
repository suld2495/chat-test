package com.chat.chat.domain.user.dto;

import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.entity.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 응답 DTO
 */
@Getter
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private UserStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSeenAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Entity to DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .status(user.getStatus())
                .lastSeenAt(user.getLastSeenAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
