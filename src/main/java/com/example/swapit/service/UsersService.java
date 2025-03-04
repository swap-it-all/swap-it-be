package com.example.swapit.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.dto.FcmTokenDto;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;

public interface UsersService {
	UserPageDto getMyPage();

	UserPageDto getAnotherUserPage(Long userId);

	void updateProfileImage(MultipartFile file);

	void updateNickname(UserNicknameDto dto);

	void updateFcmToken(FcmTokenDto dto);
}