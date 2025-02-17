package com.example.swapit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

	private final @Lazy TaskScheduler taskScheduler;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue") // 구독 채널
			.setHeartbeatValue(new long[] {10000, 20000}) // 10초, 20초 간격으로 하트비트 전송
			.setTaskScheduler(this.taskScheduler);
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 보낼 prefix
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOrigins("*"); // cors
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registry) {
		registry.interceptors(webSocketChannelInterceptor);
	}
}