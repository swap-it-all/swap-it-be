package com.example.swapit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/user/auth/refresh")
	public ResponseEntity<?> refresh(@RequestHeader("Authorization") String token) {
		return authService.refresh(token);
	}

	@GetMapping("/api/user/auth/my")
	public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
		return authService.getUserInfo(token);
	}

	@GetMapping("/auth/kakao/callback")
	public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code) {
		return authService.kakaoLogin(code);
	}
}
