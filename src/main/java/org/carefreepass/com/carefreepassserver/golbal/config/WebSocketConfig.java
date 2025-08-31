package org.carefreepass.com.carefreepassserver.golbal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지를 전송하는 브로커 활성화
        config.enableSimpleBroker("/topic", "/queue");
        // 클라이언트에서 서버로 메시지 전송 시 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")  // CORS 설정 (개발용)
                .withSockJS();  // SockJS 사용 (WebSocket 미지원 브라우저 대응)
    }
}