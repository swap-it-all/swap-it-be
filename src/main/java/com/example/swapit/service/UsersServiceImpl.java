package com.example.swapit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;
	private final UsersRepository usersRepository;

	@Override
	public void updateProfileImage(MultipartFile file) {
		Users user = currentUserService.getCurrentUser();
		user.updateProfileImageUrl(awsS3Service.updateUserProfileImage(user, file));
	}

	@Override
	public void updateNickname(UserNicknameDto dto) {
		Users user = currentUserService.getCurrentUser();
		user.updateNickname(dto.getNickname());
		usersRepository.save(user);
	}
}