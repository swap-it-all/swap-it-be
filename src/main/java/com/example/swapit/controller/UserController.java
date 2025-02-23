package com.example.swapit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

	private final UsersService usersService;

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