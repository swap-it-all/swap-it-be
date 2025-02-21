package com.example.swapit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.TradesRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;
	private final UsersRepository usersRepository;
	private final GoodsRepository goodsRepository;
	private final TradesRepository tradesRepository;
	private final ReviewRepository reviewRepository;

	@Override
	@Transactional(readOnly = true)
	public UserPageDto getUserMyPage(Long userId) {
		Users user = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Users me = currentUserService.getCurrentUser();
		boolean isMyPage = (user.getUsersId().equals(me.getUsersId()));

		long totalUsersGoodsCount = goodsRepository.countByUser(user);
		long completedSwapCount = tradesRepository.countByCompletedTradesByUser(user);
		double averageRating = reviewRepository.averageRatingByReviewee(user);
		List<ReviewDto> reviews = reviewRepository.findTop3ByRevieweeOrderByCreatedAtDesc(user)
			.stream()
			.map(ReviewDto::of)
			.toList();

		return new UserPageDto(
			user.getUsersId(), user.getNickname(),
			(isMyPage ? user.getEmail() : null),
			user.getProfileImageUrl(), totalUsersGoodsCount, completedSwapCount, averageRating, reviews
		);
	}

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