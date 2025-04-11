package com.example.swapit.config.websocket;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtProvider jwtProvider;
	private final UsersRepository usersRepository;
	private static final String SUBSCRIBED_SET_KEY = "subscribedSet";

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// HEARTBEAT 메시지는 별도로 처리
		if (accessor.getCommand() == null && "HEARTBEAT".equals(accessor.getMessageType().name())) {
			return message; // 그냥 메시지를 반환하여 계속 정상 처리되도록 함
		}

		StompCommand command = accessor.getCommand();
		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

		switch (command) {
			case CONNECT -> handleConnect(accessor, token);
			case SUBSCRIBE -> handleSubscribe(accessor);
			case UNSUBSCRIBE -> handleUnsubscribe(accessor);
			case SEND -> {
				return handleSend(message, accessor);
			}
		}
		return message;
	}

	private void handleConnect(StompHeaderAccessor accessor, String token) {
		String onlyToken = token.replace("Bearer ", "");
		validateToken(onlyToken);
		Users user = usersRepository.findByEmail(jwtProvider.getEmailFromToken(onlyToken))
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		setSessionFromPrincipal(accessor, user.getUsersId());

		// 세션 속성에 빈 구독 set 추가 : 순서 상관없을 것 같아서 일단 set으로 설정
		accessor.getSessionAttributes().put(SUBSCRIBED_SET_KEY, new HashSet<String>());

		log.debug("[CONNECT] userId={}, sessionId={}, token={}", user.getUsersId(), accessor.getSessionId(), token);
	}

	private void handleSubscribe(StompHeaderAccessor accessor) {
		setPrincipalFromSession(accessor);
		String dest = accessor.getDestination();

		Set<String> subscribedList = getOrInitSubscribedSet(accessor);
		if (!subscribedList.contains(dest)) {
			subscribedList.add(dest);
		}

		log.debug("[SUBSCRIBE] userId={}, sessionId={}, destination={}", accessor.getUser().getName(),
			accessor.getSessionId(), dest);
	}

	private void handleUnsubscribe(StompHeaderAccessor accessor) {
		setPrincipalFromSession(accessor);
		String dest = accessor.getDestination();

		Set<String> subscribedList = getOrInitSubscribedSet(accessor);
		subscribedList.remove(dest);

		log.debug("[UNSUBSCRIBE] userId={}, sessionId={}, destination={}", accessor.getUser().getName(),
			accessor.getSessionId(), dest);
	}

	private Message<?> handleSend(Message<?> message, StompHeaderAccessor accessor) {
		setPrincipalFromSession(accessor);
		String dest = accessor.getDestination();

		Set<String> subscribedList = getOrInitSubscribedSet(accessor);
		if (!subscribedList.contains(dest)) {
			log.warn("[SEND 차단] userId={}, 세션={}, destination={} → 구독되지 않은 대상", accessor.getUser().getName(),
				accessor.getSessionId(), dest);
		}

		log.debug("[SEND] userId={}, sessionId={}, destination={}", accessor.getUser().getName(),
			accessor.getSessionId(), dest);

		Map<String, Object> newHeaders = new HashMap<>(accessor.getMessageHeaders());
		newHeaders.put("simpUser", accessor.getUser());
		return MessageBuilder.createMessage(message.getPayload(), new MessageHeaders(newHeaders));
	}

	private Set<String> getOrInitSubscribedSet(StompHeaderAccessor accessor) {
		Map<String, Object> session = accessor.getSessionAttributes();
		return (Set<String>)session.computeIfAbsent(SUBSCRIBED_SET_KEY, k -> new HashSet<String>());
	}

	private void validateToken(String token) {
		if (!jwtProvider.validateToken(token)) {
			log.error("[CONNECT 오류] JWT 검증 실패");
			throw new CustomException(ErrorCode.VALIDATION_FAIL);
		}
	}

	private void setSessionFromPrincipal(StompHeaderAccessor accessor, Long userId) {
		Principal principal = new StompPrincipal(userId.toString()); // Principal 생성
		// Principal을 세션에 저장
		Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
		if (sessionAttributes != null) {
			sessionAttributes.put("principal", principal);
		}

		accessor.setUser(principal); // STOMP 메시지에 Principal 설정
		accessor.getSessionAttributes().put("simpUser", principal);
		log.info("[CONNECT] Principal 설정 완료: userId = {}", userId);
	}

	private void setPrincipalFromSession(StompHeaderAccessor accessor) {
		Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
		if (sessionAttributes == null || !sessionAttributes.containsKey("principal")) {
			log.error("[{} 오류] Principal 이 없습니다! 세션 ID: {}", accessor.getCommand(), accessor.getSessionId());
			throw new CustomException(ErrorCode.WEBSOCKET_UNAUTHORIZED_ACCESS);
		}

		Principal principal = (Principal)sessionAttributes.get("principal");
		if (principal == null) {
			log.error("[{} 오류] Principal null 입니다! 세션 ID: {}", accessor.getCommand(), accessor.getSessionId());
			throw new CustomException(ErrorCode.WEBSOCKET_UNAUTHORIZED_ACCESS);
		}

		accessor.setUser(principal);
		accessor.getSessionAttributes().put("simpUser", principal);
	}
}