package com.chat.chat.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 표준 에러 응답 DTO
 * 모든 API 에러 응답에서 일관된 형식을 제공합니다.
 */
@Getter
@Builder
public class ErrorResponse {

    /**
     * HTTP 상태 코드
     */
    private final int status;

    /**
     * 에러 코드 (비즈니스 로직용)
     */
    private final String code;

    /**
     * 사용자에게 표시할 에러 메시지
     */
    private final String message;

    /**
     * 상세 에러 메시지 (디버깅용, 옵션)
     */
    private final String details;

    /**
     * 에러가 발생한 API 경로
     */
    private final String path;

    /**
     * 에러 발생 시각
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 유효성 검증 에러 목록 (옵션)
     */
    private final List<ValidationError> validationErrors;

    /**
     * 유효성 검증 에러 상세 정보
     */
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }

    /**
     * 기본 에러 응답 생성
     */
    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 상세 정보를 포함한 에러 응답 생성
     */
    public static ErrorResponse of(int status, String code, String message, String details, String path) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .details(details)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 유효성 검증 에러 응답 생성
     */
    public static ErrorResponse of(int status, String code, String message, String path,
                                   List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
    }
}
