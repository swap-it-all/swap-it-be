package com.example.swapit.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.swapit.config.security.oauth2.OAuth2LoginSuccessHandler;
import com.example.swapit.config.security.oauth2.PrincipalOauth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final PrincipalOauth2UserService principalOauth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@SuppressWarnings("checkstyle:Indentation")
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(principalOauth2UserService)) // principalOauth2UserService를 통해 사용자 정보 처리, 로그인 성공 시 연결됨
				.successHandler(oAuth2LoginSuccessHandler)
			);

		return http.build();
	}
}
