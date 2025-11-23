package com.chat.chat.domain.user.service;

import com.chat.chat.common.exception.EntityNotFoundException;
import com.chat.chat.common.exception.ErrorCode;
import com.chat.chat.common.exception.InvalidValueException;
import com.chat.chat.domain.user.dto.UserCreateRequest;
import com.chat.chat.domain.user.dto.UserResponse;
import com.chat.chat.domain.user.entity.User;
import com.chat.chat.domain.user.entity.UserStatus;
import com.chat.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 사용자 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 생성 (간편 닉네임 가입 지원)
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        UUID userId = request.getId() != null ? request.getId() : UUID.randomUUID();
        String nickname = request.getNickname();
        String email = resolveEmail(request, nickname);

        if (userRepository.existsByEmail(email)) {
            throw new InvalidValueException(ErrorCode.USER_ALREADY_EXISTS,
                    "이미 등록된 이메일입니다: " + email);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new InvalidValueException(ErrorCode.USER_ALREADY_EXISTS,
                    "이미 사용중인 닉네임입니다: " + nickname);
        }

        User user = User.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        User savedUser = userRepository.save(user);
        log.info("👤 User created: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 ID로 조회
     */
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + userId));

        return UserResponse.from(user);
    }

    /**
     * 사용자 이메일로 조회
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + email));

        return UserResponse.from(user);
    }

    /**
     * 닉네임으로 사용자 검색
     */
    public List<UserResponse> searchUsersByNickname(String keyword) {
        return userRepository.searchByNickname(keyword)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 상태 업데이트
     */
    @Transactional
    public UserResponse updateUserStatus(UUID userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.updateStatus(status);
        log.info("✅ User status updated: {} -> {}", userId, status);

        return UserResponse.from(user);
    }

    /**
     * 프로필 업데이트
     */
    @Transactional
    public UserResponse updateProfile(UUID userId, String nickname, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                throw new InvalidValueException(ErrorCode.USER_ALREADY_EXISTS,
                        "이미 사용중인 닉네임입니다: " + nickname);
            }
        }

        user.updateProfile(nickname, profileImageUrl);
        log.info("🛠️ User profile updated: {}", userId);

        return UserResponse.from(user);
    }

    /**
     * 사용자 비활성화
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.deactivate();
        log.info("🛑 User deactivated: {}", userId);
    }

    /**
     * 내부에서 User Entity 조회
     */
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + userId));
    }

    private String resolveEmail(UserCreateRequest request, String nickname) {
        if (StringUtils.hasText(request.getEmail())) {
            return request.getEmail();
        }
        return generateGuestEmail(nickname);
    }

    private String generateGuestEmail(String nickname) {
        String base = StringUtils.hasText(nickname)
                ? nickname.replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                : "guest";
        if (!StringUtils.hasText(base)) {
            base = "guest";
        }
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return base + "-" + suffix + "@chat.local";
    }
}
