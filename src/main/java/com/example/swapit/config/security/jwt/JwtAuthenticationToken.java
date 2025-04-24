package com.example.swapit.config.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * 인증 정보 객체
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
	private final String token;
	private final String userId;

	public JwtAuthenticationToken(String token, String userId) {
		super(null);
		this.token = token;
		this.userId = userId;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}
}
