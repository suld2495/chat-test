package com.chat.chat.websocket.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 이벤트 리스너
 * 연결/해제 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * WebSocket 연결 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("🔌 WebSocket connected: sessionId={}", sessionId);
    }

    /**
     * WebSocket 연결 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 세션 속성에서 사용자 정보 가져오기
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String chatRoomId = (String) headerAccessor.getSessionAttributes().get("chatRoomId");

        if (userId != null && chatRoomId != null) {
            log.info("🔌 WebSocket disconnected: sessionId={}, userId={}, chatRoomId={}",
                    sessionId, userId, chatRoomId);

            // 필요시 퇴장 메시지 전송 가능
            // ChatMessageDto leaveMessage = ...
            // messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, leaveMessage);
        } else {
            log.info("🔌 WebSocket disconnected: sessionId={}", sessionId);
        }
    }
}
