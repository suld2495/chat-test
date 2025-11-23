package com.chat.chat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 각 비즈니스 로직 에러에 대한 코드와 메시지를 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (1000번대)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C007", "인증이 필요합니다."),

    // User Errors (2000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U003", "잘못된 사용자 인증 정보입니다."),

    // Chat Room Errors (3000번대)
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "R002", "이미 존재하는 채팅방입니다."),
    NOT_CHAT_ROOM_PARTICIPANT(HttpStatus.FORBIDDEN, "R003", "채팅방 참여자가 아닙니다."),
    CHAT_ROOM_FULL(HttpStatus.BAD_REQUEST, "R004", "채팅방이 가득 찼습니다."),

    // Message Errors (4000번대)
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메시지를 찾을 수 없습니다."),
    MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "M002", "메시지 전송에 실패했습니다."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "M003", "잘못된 메시지 내용입니다."),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "M004", "메시지가 너무 깁니다."),

    // WebSocket Errors (5000번대)
    WEBSOCKET_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "W001", "WebSocket 연결에 실패했습니다."),
    WEBSOCKET_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "W002", "WebSocket 세션을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
