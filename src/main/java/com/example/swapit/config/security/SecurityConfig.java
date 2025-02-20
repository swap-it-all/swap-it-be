package com.example.swapit.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.swapit.config.security.jwt.JwtAuthenticationFilter;
import com.example.swapit.config.security.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final JwtProvider jwtProvider;
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(
				auth -> auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/docs/**")
					.permitAll()
					.requestMatchers("/api/user/auth/refresh", "/api/user/auth/logout", "/ws/**")
					.permitAll()
					.requestMatchers("/api/all/**")
					.permitAll() // 누구나 접근 가능
					.requestMatchers("/api/user/**")
					.hasRole("USER") // USER만 접근 가능
					.anyRequest()
					.authenticated() // 그 외 요청은 인증 필요
			)
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userDetailsService),
				UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling((exception) -> exception.authenticationEntryPoint(
				(request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized")));

		return http.build();
	}
}
