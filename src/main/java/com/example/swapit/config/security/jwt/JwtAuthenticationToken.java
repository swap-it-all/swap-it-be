package com.example.swapit.config.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * 인증 정보 객체
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
	private final String email;
	private final String token;

	public JwtAuthenticationToken(String email, String token) {
		super(null);
		this.email = email;
		this.token = token;
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
