package com.chat.chat.domain.message.entity;

/**
 * 메시지 타입
 */
public enum MessageType {
    /**
     * 텍스트 메시지
     */
    TEXT,

    /**
     * 이미지 메시지
     */
    IMAGE,

    /**
     * 파일 메시지
     */
    FILE,

    /**
     * 시스템 메시지 (입장, 퇴장 등)
     */
    SYSTEM
}
