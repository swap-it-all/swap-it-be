package com.example.swapit.config.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.dto.TokenDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthenticationProvider: 인증과 관련된 인터페이스. 사용자의 인증을 수행하고, 인증된 사용자 객체를 생성하여 스프링 시큐리티에 전달
 */
@Slf4j
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
		String userId = getIdFromToken(token);
		return new JwtAuthenticationToken(token, userId);
	}

	/**
	 * Access Token, Refresh Token 생성
	 * @param userId
	 * @return
	 */
	public TokenDTO createToken(String userId) {
		Claims claims = Jwts.claims().setSubject(userId);
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

		return TokenDTO.builder().accessToken(accessToken).refreshToken(refreshToken).key(userId).build();
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
			log.warn("[JWT 에러][Access] 만료된 토큰입니다. token={}", token, e);
		} catch (UnsupportedJwtException e) {
			log.warn("[JWT 에러][Access] 지원되지 않는 형식입니다. token={}", token, e);
		} catch (MalformedJwtException e) {
			log.warn("[JWT 에러][Access] 잘못된 형식입니다. token={}", token, e);
		} catch (SignatureException e) {
			log.warn("[JWT 에러][Access] 서명 검증 실패. token={}", token, e);
		} catch (IllegalArgumentException e) {
			log.warn("[JWT 에러][Access] 잘못된 인자입니다. token={}", token, e);
		} catch (JwtException e) {
			log.warn("[JWT 에러][Access] 기타 JWT 처리 오류. token={}", token, e);
		}

		return false;
	}

	/**
	 * 토큰에서 사용자 이메일 추출
	 * @param token
	 * @return
	 */
	public String getIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.getSubject();
	}

	public String getIdFromRefreshToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				.setSigningKey(REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token)
				.getBody();

			return claims.getSubject();

		} catch (ExpiredJwtException e) {
			log.warn("[JWT 에러][Refresh] 만료된 토큰입니다. token={}", token, e);
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			log.warn("[JWT 에러][Refresh] 지원되지 않는 형식입니다. token={}", token, e);
			throw new CustomException(ErrorCode.UNSUPPORTED_JWT);
		} catch (MalformedJwtException e) {
			log.warn("[JWT 에러][Refresh] 잘못된 형식입니다. token={}", token, e);
			throw new CustomException(ErrorCode.MALFORMED_JWT);
		} catch (SignatureException e) {
			log.warn("[JWT 에러][Refresh] 서명 검증 실패. token={}", token, e);
			throw new CustomException(ErrorCode.INVALID_JWT_SIGNATURE);
		} catch (IllegalArgumentException e) {
			log.warn("[JWT 에러][Refresh] 잘못된 인자입니다. token={}", token, e);
			throw new CustomException(ErrorCode.JWT_PARSING_FAILED);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TokenDTO.class.isAssignableFrom(authentication);
	}
}