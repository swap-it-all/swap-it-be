package com.example.swapit.config.security.oauth2;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.config.security.jwt.JwtService;
import com.example.swapit.domain.dto.TokenDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 로그인 성공 시 실행되는 후처리 로직
 * 생성된 토큰을 클라이언트에 반환
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final JwtService jwtService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		// OAuth2 로그인이 성공했을 때의 추가 작업을 수행
		// 여기에서는 JWT 토큰을 발급하고 형식에 맞게 return

		PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
		TokenDTO token = jwtProvider.createToken(principalDetails.getUsers().getEmail());

		jwtService.saveRefreshToken(principalDetails.getUsers(), token.getRefreshToken());

		// 응답 헤더에 JWT 토큰 추가
		response.addHeader("Authorization", "Bearer " + token);

		// JWT 토큰을 response에 담아서 전송
		response.setContentType("application/json");
		response.getWriter().write("{\"token\": \"" + token + "\"}");
	}
}
