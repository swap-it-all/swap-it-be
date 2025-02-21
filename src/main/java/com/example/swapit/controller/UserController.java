package com.example.swapit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.config.security.CustomUserDetails;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

	private final UsersService usersService;

	// todo : 현재 쓰고 있는 곳이 아예 없는지 확인 후, 삭제 필수.
	// 사용자 정보 가져오는 임시 코드
	@GetMapping("/user/me")
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

	@GetMapping("/user/auth/info/my")
	public ApiResponse<UserPageDto> getMyPage() {
		return ApiResponse.success(usersService.getMyPage());
	}

	@GetMapping("/all/auth/info/{userId}")
	public ApiResponse<UserPageDto> getAnotherUserPage(@PathVariable Long userId) {
		return ApiResponse.success(usersService.getAnotherUserPage(userId));
	}

	@PatchMapping("/user/auth/profile/image")
	public ApiResponse<Void> updateProfileImage(MultipartFile image) {
		usersService.updateProfileImage(image);
		return ApiResponse.success();
	}

	@PatchMapping("/user/auth/profile/nickname")
	public ApiResponse<Void> updateProfileNickname(@Valid @RequestBody UserNicknameDto dto) {
		usersService.updateNickname(dto);
		return ApiResponse.success();
	}
}