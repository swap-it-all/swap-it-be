package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.TradesRepository;
import com.example.swapit.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

	@InjectMocks
	private UsersServiceImpl usersService;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private AwsS3Service awsS3Service;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private UsersRepository usersRepository;

	private Users user;

	@BeforeEach
	void setUp() {
		user = Users.builder()
			.usersId(1L)
			.nickname("nickname")
			.email("test@example.com")
			.profileImageUrl("http://example.com/image.jpg")
			.loginInfo("google")
			.build();
	}

	@Test
	@DisplayName("마이페이지 조회 성공")
	void getMyPage_Success() {
		// given
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(goodsRepository.countByUser(user)).thenReturn(5L); // 상품 5개
		when(tradesRepository.countByCompletedTradesByUser(user)).thenReturn(3L); // 거래 완료 3개
		when(reviewRepository.averageRatingByReviewee(user)).thenReturn(4.5); // 평균 평점 4.5
		when(reviewRepository.findTop3ByRevieweeOrderByCreatedAtDesc(user)).thenReturn(List.of());

		// when
		UserPageDto result = usersService.getMyPage();

		// then
		assertNotNull(result);
		assertEquals(1L, result.getUsersId());
		assertEquals("nickname", result.getNickname());
		assertEquals("test@example.com", result.getEmail());
		assertEquals(5L, result.getTotalGoodsCount());
		assertEquals(3L, result.getCompletedSwapCount());
		assertEquals(4.5, result.getRatingAverage());
	}

	@Test
	@DisplayName("다른 회원 유저페이지 조회 성공")
	void getAnotherUserPage_Success() {
		// given
		Users currentUser = Users.builder().usersId(2L).build();
		when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
		when(goodsRepository.countByUser(user)).thenReturn(5L); // 상품 5개
		when(tradesRepository.countByCompletedTradesByUser(user)).thenReturn(3L); // 거래 완료 3개
		when(reviewRepository.averageRatingByReviewee(user)).thenReturn(4.5); // 평균 평점 4.5
		when(reviewRepository.findTop3ByRevieweeOrderByCreatedAtDesc(user)).thenReturn(List.of());

		// when
		UserPageDto result = usersService.getAnotherUserPage(1L);

		// then
		assertNotNull(result);
		assertEquals(1L, result.getUsersId());
		assertEquals("nickname", result.getNickname());
		assertNull(result.getEmail());
		assertEquals(5L, result.getTotalGoodsCount());
		assertEquals(3L, result.getCompletedSwapCount());
		assertEquals(4.5, result.getRatingAverage());
	}

	@Test
	@DisplayName("유저 페이지 가져오기 실패 - 유저를 찾을 수 없음")
	void getUserMyPage_Fail_byUserNotFound() {
		// Given
		when(usersRepository.findById(99L)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CustomException.class, () -> usersService.getAnotherUserPage(99L));
	}

	@Test
	@DisplayName("유저 프로필 사진 업데이트 성공")
	void updateProfileImage_Success() {
		// given
		MultipartFile mockFile = new MockMultipartFile(
			"file", "profile.jpg", "image/jpeg", new byte[] {1, 2, 3, 4}
		);

		String newImageUrl = "https://s3.amazonaws.com/bucket/images/users/1";

		// Mock 객체 설정
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(awsS3Service.updateUserProfileImage(any(Users.class), any(MultipartFile.class)))
			.thenReturn(newImageUrl);

		// WHEN: 프로필 이미지를 업데이트
		usersService.updateProfileImage(mockFile);

		// THEN: 프로필 이미지 URL이 변경되었는지 확인
		assertEquals(newImageUrl, user.getProfileImageUrl());

		// Mock 객체가 올바르게 호출되었는지 검증
		verify(currentUserService, times(1)).getCurrentUser();
		verify(awsS3Service, times(1)).updateUserProfileImage(user, mockFile);
	}
}