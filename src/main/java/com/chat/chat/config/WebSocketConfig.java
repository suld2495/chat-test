package com.chat.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * STOMP 프로토콜을 사용한 실시간 메시징
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 WebSocket에 연결할 엔드포인트
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS(); // SockJS 폴백 옵션 활성화

        log.info("✅ WebSocket STOMP endpoint registered: /ws-chat");
    }

    /**
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커가 처리할 prefix
        // 클라이언트가 구독할 destination prefix
        registry.enableSimpleBroker(
                "/topic",   // 1:N 브로드캐스트 (채팅방 전체)
                "/queue"    // 1:1 메시지 (개인)
        );

        // 클라이언트가 메시지를 보낼 때 사용할 prefix
        // @MessageMapping의 destination에 자동으로 prefix 추가
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용할 prefix
        registry.setUserDestinationPrefix("/user");

        log.info("✅ Message broker configured: /topic, /queue, /app, /user");
    }
}
