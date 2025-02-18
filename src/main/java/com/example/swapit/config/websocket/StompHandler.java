package com.example.swapit.config.websocket;

import java.util.HashMap;
import java.util.Map;

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

@Configuration
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtProvider jwtProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
			StompCommand.SEND.equals(accessor.getCommand())) {
			validateToken(accessor);
		}

		MessageHeaders headers = accessor.getMessageHeaders();
		Map<String, Object> newHeaders = new HashMap<>(headers);
		newHeaders.put("simpUser", accessor.getUser());

		return MessageBuilder.createMessage(message.getPayload(), new MessageHeaders(newHeaders));
	}

	private void validateToken(StompHeaderAccessor accessor) {
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
