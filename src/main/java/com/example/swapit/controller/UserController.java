package com.example.swapit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.config.security.CustomUserDetails;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.service.UsersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UsersService usersService;

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

	@PatchMapping("/api/user/auth/profile/image")
	public ApiResponse<Void> updateProfileImage(MultipartFile image) {
		usersService.updateProfileImage(image);
		return ApiResponse.success();
	}

	@PatchMapping("/api/user/auth/profile/nickname")
	public ApiResponse<Void> updateProfileNickname(@RequestBody UserNicknameDto dto) {
		usersService.updateNickname(dto);
		return ApiResponse.success();
	}
}