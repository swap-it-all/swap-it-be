package com.example.swapit.config.websocket;

import java.util.HashMap;
import java.util.Map;

import java.security.Principal;

import java.security.Principal;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtProvider jwtProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
		log.info("액세스 토큰 : {}", token);

		if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
			StompCommand.SEND.equals(accessor.getCommand())) {
			validateToken2(accessor);
		} else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
		String destination = accessor.getDestination();
		String sessionId = accessor.getSessionId();

		// handshake에서 가져온 principal 저장
		Principal principal = (Principal)accessor.getSessionAttributes().get("principal");
		accessor.setUser(principal);

		if (principal == null) {
			log.error("[SUBSCRIBE 오류] Principal이 null입니다! 세션 id: {}, 구독 채널: {}", sessionId, destination);
		} else {
			log.info("[SUBSCRIBE] 유저 id : {}, 세션 id : {}, 구독 채널 : {}", principal.getName(), sessionId, destination);
		}
	}

		MessageHeaders headers = accessor.getMessageHeaders();
		Map<String, Object> newHeaders = new HashMap<>(headers);
		newHeaders.put("simpUser", accessor.getUser());

		return MessageBuilder.createMessage(message.getPayload(), new MessageHeaders(newHeaders));
	}

	private void validateToken(String token) {
		token = token.replace("Bearer ", "");
		if (!jwtProvider.validateToken(token)) {
			throw new CustomException(ErrorCode.VALIDATION_FAIL);
	}

	private void validateToken2(StompHeaderAccessor accessor) {
		String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
		boolean validated = jwtProvider.validateToken(token);

		if (!validated) {
			throw new CustomException(ErrorCode.VALIDATION_FAIL);
		}

		String email = jwtProvider.getEmailFromToken(token);
		StompPrincipal stompPrincipal = new StompPrincipal(email);
		accessor.setUser(stompPrincipal);
	}
}