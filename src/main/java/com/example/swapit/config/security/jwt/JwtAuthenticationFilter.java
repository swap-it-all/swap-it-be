package com.example.swapit.config.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.swapit.config.security.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final CustomUserDetailsService userDetailsService;

	private static final List<String> EXCLUDE_URLS = Arrays.asList(
		"/api/user/auth/refresh",
		"/api/all/auth/login/google",
		"/api/all/auth/login/kakao",
		"/api/user/auth/logout",
		"/api/all/auth/info",
		"/api/all/sample",
		"api/all/goods",
		"/ws"
	);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return EXCLUDE_URLS.stream()
			.anyMatch(exclude -> request.getServletPath().startsWith(exclude));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (jwtProvider.validateToken(token)) {
				String username = jwtProvider.getEmailFromToken(token);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		filterChain.doFilter(request, response);
	}
}