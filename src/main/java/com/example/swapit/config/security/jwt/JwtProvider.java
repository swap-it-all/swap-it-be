package com.example.swapit.config.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.example.swapit.domain.dto.TokenDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * AuthenticationProvider: 인증과 관련된 인터페이스. 사용자의 인증을 수행하고, 인증된 사용자 객체를 생성하여 스프링 시큐리티에 전달
 */
@Component
@RequiredArgsConstructor
public class JwtProvider implements AuthenticationProvider {

	@Value("${jwt.secret.access-token}")
	private String ACCESS_SECRET_KEY;

	@Value("${jwt.secret.refresh-token}")
	private String REFRESH_SECRET_KEY;

	@Value("${jwt.secret.access-expiration}")
	private long ACCESS_TOKEN_VALID_TIME;

	@Value("${jwt.secret.refresh-expiration}")
	private long REFRESH_TOKEN_VALID_TIME;

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

		Key accessKey = Keys.hmacShaKeyFor(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
		Key refreshKey = Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8));

		String accessToken = Jwts.builder()
			.setClaims(claims) // 정보 저장
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
			.signWith(accessKey, SignatureAlgorithm.HS256)
			.compact();

		String refreshToken = Jwts.builder()
			.setClaims(claims) // 정보 저장
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
			.signWith(refreshKey, SignatureAlgorithm.HS256)
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
			Jwts.parserBuilder()
				.setSigningKey(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
				.build()
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
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject();
	}

	public String getEmailFromRefreshToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject();
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TokenDTO.class.isAssignableFrom(authentication);
	}
}