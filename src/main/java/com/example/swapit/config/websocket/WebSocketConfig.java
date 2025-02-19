package com.example.swapit.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompHandler stompHandler; // jwt 인증
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10);
		scheduler.setThreadNamePrefix("TaskScheduler-");
		scheduler.initialize();
		return scheduler;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue", "/user") // 일반 구독 채널
			.setHeartbeatValue(new long[] {30000, 60000}) // 30초, 60초 간격으로 하트비트 전송
			.setTaskScheduler(taskScheduler());
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 보낼 prefix
		registry.setUserDestinationPrefix("/user"); // 특정 사용자에게 보낼 때 사용하는 prefix
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			// .addInterceptors(jwtHandshakeInterceptor) // jwt 인증 인터셉터 추가
			.setAllowedOrigins("*"); // cors
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}
}