package com.example.swapit.config.websocket;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtProvider jwtProvider;
	private final UsersRepository usersRepository;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {
		HttpHeaders headers = request.getHeaders();
		String token = headers.getFirst(HttpHeaders.AUTHORIZATION).replace("Bearer ", "");

		if (jwtProvider.validateToken(token)) {
			String email = jwtProvider.getEmailFromToken(token);
			Users user = usersRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
			log.info("[웹소켓 HANDSHAKE 시도] 유저 ID : {} ({})", user.getUsersId(), user.getNickname());

			// // 웹소켓 세션에 유저 정보 저장
			// attributes.put("userId", user.getUsersId());

			// principal 설정
			attributes.put("principal", new StompPrincipal(user.getUsersId().toString()));

			return true; // 핸드셰이크 성공
		}

		// 인증 실패 시, Websocket 연결 거부
		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {
	}
}