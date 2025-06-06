package com.example.swapit.service.chat;

import java.security.Principal;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketEventListener {
	private final StompSubscriptionService subscriptionService;

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();

		if (principal == null) {
			log.warn("[DISCONNECT] Principal 없음 → 세션만 종료: sessionId={}", accessor.getSessionId());
			return;
		}

		String userId = principal.getName();

		subscriptionService.removeUser(Long.valueOf(userId)); // userId 기반 정리
		log.info("[DISCONNECT] 유저 연결 종료: userId={}, sessionId={}", userId, accessor.getSessionId());
	}
}
