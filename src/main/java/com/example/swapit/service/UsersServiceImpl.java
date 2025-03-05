package com.example.swapit.service;

import java.util.List;
import java.util.Optional;

import com.example.swapit.domain.GoodsTradeStatus;
import com.example.swapit.domain.TradeStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.FcmToken;
import com.example.swapit.domain.GoodsTradeStatus;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.FcmTokenDto;
import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.repository.FcmTokenRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;
	private final UsersRepository usersRepository;
	private final GoodsRepository goodsRepository;
	private final ReviewRepository reviewRepository;
	private final FcmTokenRepository fcmTokenRepository;

	@Override
	public UserPageDto getMyPage() {
		Users user = currentUserService.getCurrentUser();

		long totalUsersGoodsCount = goodsRepository.countByUser(user);
		long completedSwapCount = goodsRepository.countByUserAndGoodsTradeStatus(user, GoodsTradeStatus.SOLDOUT);
		double averageRating = reviewRepository.averageRatingByReviewee(user);
		List<ReviewDto> reviewsTop5 = reviewRepository.findTop5ByRevieweeOrderByCreatedAtDesc(user)
			.stream()
			.map(ReviewDto::of)
			.toList();
		long totalReviewCount = reviewRepository.countByReviewee(user);

		// 프로필 이미지가 내부 저장 이미지일 때, 처리
		String profileImageUrl = user.getProfileImageUrl();
		if (!profileImageUrl.startsWith("http")) {
			profileImageUrl = awsS3Service.generatePreSignedImageUrl(profileImageUrl.trim());
		}

		return new UserPageDto(
			user.getUsersId(), user.getNickname(), user.getEmail(), profileImageUrl,
			totalUsersGoodsCount, completedSwapCount, averageRating, totalReviewCount, reviewsTop5
		);
	}

	@Override
	public UserPageDto getAnotherUserPage(Long userId) {
		Users user = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		long totalUsersGoodsCount = goodsRepository.countByUser(user);
		long completedSwapCount = goodsRepository.countByUserAndGoodsTradeStatus(user, GoodsTradeStatus.SOLDOUT);
		double averageRating = reviewRepository.averageRatingByReviewee(user);
		List<ReviewDto> reviewsRecentlyTop5 = reviewRepository.findTop5ByRevieweeOrderByCreatedAtDesc(user)
			.stream()
			.map(ReviewDto::of)
			.toList();
		long totalReviewCount = reviewRepository.countByReviewee(user);

		// 프로필 이미지가 내부 저장 이미지일 때, 처리
		String profileImageUrl = user.getProfileImageUrl();
		if (!profileImageUrl.startsWith("http")) {
			profileImageUrl = awsS3Service.generatePreSignedImageUrl(profileImageUrl.trim());
		}

		return new UserPageDto(
			user.getUsersId(), user.getNickname(), null, profileImageUrl,
			totalUsersGoodsCount, completedSwapCount, averageRating, totalReviewCount, reviewsRecentlyTop5
		);
	}

	@Override
	@Transactional
	public void updateProfileImage(MultipartFile file) {
		Users user = currentUserService.getCurrentUser();
		user.updateProfileImageUrl(awsS3Service.updateUserProfileImage(user, file));
	}

	@Override
	@Transactional
	public void updateNickname(UserNicknameDto dto) {
		Users user = currentUserService.getCurrentUser();
		user.updateNickname(dto.getNickname());
		usersRepository.save(user);
	}

	@Override
	@Transactional
	public void updateFcmToken(FcmTokenDto dto) {
		Users user = currentUserService.getCurrentUser();

		// 일반적으로 fcm 토큰은 길이 검증만 함.
		if (!isValidFcmToken(dto.getFcmToken())) {
			throw new CustomException(ErrorCode.FCM_TOKEN_INVALID);
		}

		Optional<FcmToken> existFcmToken = fcmTokenRepository.findByUser(user);

		if (existFcmToken.isPresent()) {
			existFcmToken.get().setFcmToken(dto.getFcmToken());
			fcmTokenRepository.save(existFcmToken.get());
		} else {
			FcmToken newToken = FcmToken.builder()
				.user(user)
				.fcmToken(dto.getFcmToken())
				.build();
			fcmTokenRepository.save(newToken);
		}
	}

	private boolean isValidFcmToken(String token) {
		return token != null && token.length() >= 100 && token.length() <= 1000;
	}
}