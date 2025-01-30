package com.example.swapit.config.oauth;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.swapit.domain.Users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 인증 정보를 담는 객체
 */
@Getter
@RequiredArgsConstructor
public class PrincipalDetails implements OAuth2User {

	@Getter
	private Users users;
	private Map<String, Object> attributes;

	public PrincipalDetails(Users users, Map<String, Object> attributes) {
		this.users = users;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getName() {
		return users.getEmail();
	}
}
