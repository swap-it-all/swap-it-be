package com.example.swapit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.Users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;

	@Override
	public void updateProfileImage(MultipartFile file) {
		Users user = currentUserService.getCurrentUser();
		user.updateProfileImageUrl(awsS3Service.updateUserProfileImage(user, file));
	}
}