package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.Users;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

	@InjectMocks
	private UsersServiceImpl usersService;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private AwsS3Service awsS3Service;

	@BeforeEach
	void setUp() {
	}

	@Test
	@DisplayName("유저 프로필 사진 업데이트 성공")
	void updateProfileImage_Success() {
		// GIVEN: 현재 로그인된 사용자와 업로드된 이미지 URL 설정
		Users testUser = Users.builder()
			.nickname("nickname")
			.email("test@example.com")
			.profileImageUrl("http://example.com/image.jpg")
			.loginInfo("google")
			.build();

		MultipartFile mockFile = new MockMultipartFile(
			"file", "profile.jpg", "image/jpeg", new byte[] {1, 2, 3, 4}
		);

		String newImageUrl = "https://s3.amazonaws.com/bucket/images/users/1";

		// Mock 객체 설정
		when(currentUserService.getCurrentUser()).thenReturn(testUser);
		when(awsS3Service.updateUserProfileImage(any(Users.class), any(MultipartFile.class)))
			.thenReturn(newImageUrl);

		// WHEN: 프로필 이미지를 업데이트
		usersService.updateProfileImage(mockFile);

		// THEN: 프로필 이미지 URL이 변경되었는지 확인
		assertEquals(newImageUrl, testUser.getProfileImageUrl());

		// Mock 객체가 올바르게 호출되었는지 검증
		verify(currentUserService, times(1)).getCurrentUser();
		verify(awsS3Service, times(1)).updateUserProfileImage(testUser, mockFile);
	}
}