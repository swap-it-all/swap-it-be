package com.example.swapit.config.oauth;

import static com.example.swapit.util.Constant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

/**
 * AuthenticationProvider: 인증과 관련된 인터페이스. 사용자의 인증을 수행하고, 인증된 사용자 객체를 생성하여 스프링 시큐리티에 전달
 */
@Component
@RequiredArgsConstructor
public class JwtProvider implements AuthenticationProvider {

	/**
	 * JwtFilter에서 authentication을 받아오는데, authentication에는 토큰이 저장되어 있음
	 * 토큰을 사용하여 유저의 아이디와 권한을 가지고 오고
	 * JwtAuthentication에 토큰, 유저, 권한을 담아 보내줌
	 *
	 * @param authentication the autentication request object.
	 * @return JwtAuthenticationToekn (유저의 정보를 담아 보내줌)
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String token = authentication.getCredentials().toString();
		// 토큰 검증 로직 추가
		if (!validateToken(token)) {
			throw new BadCredentialsException("Invalid token");
		}
		// 토큰에서 사용자 정보 추출 및 인증 객체 반환
		String email = getEmailFromToken(token);
		return new JwtAuthenticationToken(email, token);
	}

	/**
	 * Access Token, Refresh Token 생성
	 * @param email
	 * @return
	 */
	public TokenDTO createToken(String email) {
		Claims claims = Jwts.claims().setSubject(email);
		Date now = new Date();

		String accessToken = Jwts.builder()
			.setClaims(claims) // 정보 저장
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
			.signWith(SignatureAlgorithm.HS256, ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.compact();

		String refreshToken = Jwts.builder()
			.setClaims(claims) // 정보 저장
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
			.signWith(SignatureAlgorithm.HS256, REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.compact();

		return TokenDTO.builder().accessToken(accessToken).refreshToken(refreshToken).key(email).build();
	}

	/**
	 * JWT의 유효성 검증
	 * @param token
	 * @return
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.setSigningKey(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
				.parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			System.out.println("Token expired");
			return false;
		} catch (JwtException | IllegalArgumentException e) {
			System.out.println("Invalid token");
			return false;
		}
	}

	/**
	 * 토큰에서 사용자 이메일 추출
	 * @param token
	 * @return
	 */
	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parser()
			.setSigningKey(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject();
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TokenDTO.class.isAssignableFrom(authentication);
	}
}
