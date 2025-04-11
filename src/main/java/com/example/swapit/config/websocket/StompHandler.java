package com.example.swapit.config.websocket;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
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

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// HEARTBEAT 메시지는 별도로 처리
		if (accessor.getCommand() == null && "HEARTBEAT".equals(accessor.getMessageType().name())) {
			return message; // 그냥 메시지를 반환하여 계속 정상 처리되도록 함
		}

		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

		switch (accessor.getCommand()) {
			case CONNECT -> {
				String onlyToken = token.replace("Bearer ", "");
				validateToken(onlyToken);
				Users user = usersRepository.findByEmail(jwtProvider.getEmailFromToken(onlyToken))
					.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
				setSessionFromPrincipal(accessor, user.getUsersId());
				log.debug("[{}}] userId={}, sessionId={}, token={}", accessor.getCommand(), user.getUsersId(),
					accessor.getSessionId(), token);
			}
			case SUBSCRIBE -> {
				setPrincipalFromSession(accessor);
				log.debug("[{}] userId={}, sessionId={}, destination={}", accessor.getCommand(),
					accessor.getUser().getName(), accessor.getSessionId(), accessor.getDestination());
			}
			case UNSUBSCRIBE -> {
				setPrincipalFromSession(accessor);
				log.debug("[{}] userId={}, sessionId={}", accessor.getCommand(),
					accessor.getUser().getName(), accessor.getSessionId());
			}
			case SEND -> {
				setPrincipalFromSession(accessor);
				log.debug("[{}] userId={}, sessionId={}, destination={}", accessor.getCommand(),
					accessor.getUser().getName(), accessor.getSessionId(), accessor.getDestination());

				MessageHeaders headers = accessor.getMessageHeaders();
				Map<String, Object> newHeaders = new HashMap<>(headers);
				newHeaders.put("simpUser", accessor.getUser());

				return MessageBuilder.createMessage(message.getPayload(), new MessageHeaders(newHeaders));
			}
		}
		return message;
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