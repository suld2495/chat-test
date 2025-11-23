package com.chat.chat.domain.user.controller;

import com.chat.chat.domain.user.dto.UserCreateRequest;
import com.chat.chat.domain.user.dto.UserResponse;
import com.chat.chat.domain.user.entity.UserStatus;
import com.chat.chat.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 사용자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 (Supabase Auth 연동 후 호출)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("📥 POST /api/users - Create user: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 ID로 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("📥 GET /api/users/{} - Get user by ID", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 이메일로 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("📥 GET /api/users/email/{} - Get user by email", email);
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임으로 사용자 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        log.info("📥 GET /api/users/search?keyword={} - Search users", keyword);
        List<UserResponse> response = userService.searchUsersByNickname(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 상태 업데이트
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {

        UserStatus status = UserStatus.valueOf(request.get("status"));
        log.info("📥 PATCH /api/users/{}/status - Update status: {}", userId, status);

        UserResponse response = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 업데이트
     */
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {

        String nickname = request.get("nickname");
        String profileImageUrl = request.get("profileImageUrl");

        log.info("📥 PATCH /api/users/{}/profile - Update profile", userId);

        UserResponse response = userService.updateProfile(userId, nickname, profileImageUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 비활성화
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        log.info("📥 DELETE /api/users/{} - Deactivate user", userId);
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}
