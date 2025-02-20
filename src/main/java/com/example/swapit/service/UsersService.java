package com.example.swapit.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.dto.UserNicknameDto;

public interface UsersService {
	void updateProfileImage(MultipartFile file);

	void updateNickname(UserNicknameDto dto);
}