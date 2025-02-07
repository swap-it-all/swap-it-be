package com.example.swapit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.swapit.config.security.CustomUserDetails;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.UserResponseDTO;

@Controller
public class UserController {
	// 사용자 정보 가져오는 임시 코드
	@GetMapping("api/user/me")
	public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		if (customUserDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
		}
		Users user = customUserDetails.getUser();
		UserResponseDTO userResponseDTO = UserResponseDTO.builder()
			.nickname(user.getNickname())
			.email(user.getEmail())
			.profileImgUrl(user.getProfileImageUrl())
			.loginInfo(user.getLoginInfo())
			.build();
		return ResponseEntity.ok(userResponseDTO);
	}
}
