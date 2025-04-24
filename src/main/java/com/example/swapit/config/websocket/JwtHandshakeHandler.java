package com.example.swapit.config.websocket;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.repository.UsersRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

	private final JwtProvider jwtProvider;
	private final UsersRepository usersRepository;

	@Override
	protected Principal determineUser(ServerHttpRequest request,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes) {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
		String authHeader = servletRequest.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			if (jwtProvider.validateToken(token)) {
				String userId = jwtProvider.getIdFromToken(token);
				log.info("[Handshake] 유저 인증 성공: userId={}", userId);
				return new StompPrincipal(userId); // SimpUser.getName()이 이 값이 됨
			} else {
				log.warn("[Handshake] JWT 유효성 실패");
			}
		} else {
			log.warn("[Handshake] Authorization 헤더 없음");
		}

		return null; // 인증 실패 → WebSocket 연결 안 됨
	}
}
