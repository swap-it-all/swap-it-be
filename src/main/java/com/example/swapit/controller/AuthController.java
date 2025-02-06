package com.example.swapit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService; // 인터페이스 사용

	@PostMapping("/user/auth/refresh")
	public ApiResponse<TokenDTO> refresh(@RequestHeader("Authorization") String token) {
		return ApiResponse.success(authService.refresh(token));
	}

	@GetMapping("/user/auth/my")
	public ApiResponse<UserResponseDTO> getUserInfo(@RequestHeader("Authorization") String token) {
		return ApiResponse.success(authService.getUserInfo(token));
	}

	@GetMapping("/all/auth/login/google")
	public ApiResponse<TokenDTO> googleLogin(@RequestHeader("Authorization") String token) {
		return ApiResponse.success(authService.googleLogin(token));
	}

	@GetMapping("/all/auth/login/kakao")
	public ApiResponse<TokenDTO> kakaoLogin(@RequestHeader("Authorization") String token) {
		return ApiResponse.success(authService.kakaoLogin(token));
	}

	@PostMapping("/user/auth/logout")
	public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
		authService.deleteRefreshToken(token);
		return ApiResponse.success();
	}
}
