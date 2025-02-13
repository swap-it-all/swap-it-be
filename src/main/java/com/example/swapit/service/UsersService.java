package com.example.swapit.service;

import org.springframework.web.multipart.MultipartFile;

public interface UsersService {
	void updateProfileImage(MultipartFile file);
}