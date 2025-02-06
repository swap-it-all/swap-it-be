package com.example.swapit.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/user/auth/refresh")
	public ResponseEntity<?> refresh(@RequestHeader("Authorization") String token) {
		Map<String, Object> response = authService.refresh(token);

		if (!(Boolean)response.get("success")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/user/auth/my")
	public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
		Map<String, Object> response = authService.getUserInfo(token);

		if (!(Boolean)response.get("success")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/all/auth/login/google")
	public ResponseEntity<?> googleLogin(@RequestHeader("Authorization") String token) {
		Map<String, Object> response = authService.googleLogin(token);

		if (!(Boolean)response.get("success")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/all/auth/login/kakao")
	public ResponseEntity<?> kakaoLogin(@RequestHeader("Authorization") String token) {
		Map<String, Object> response = authService.kakaoLogin(token);

		if (!(Boolean)response.get("success")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/user/auth/logout")
	public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
		authService.deleteRefreshToken(token); // DB에서 Refresh Token 삭제
		return ResponseEntity.ok()
			.body(Map.of(
				"success", true,
				"message", "로그아웃에 성공하였습니다."
			));
	}
}
