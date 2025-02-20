package com.example.swapit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Reviews;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.ReviewListDto;
import com.example.swapit.domain.dto.ReviewRequestDto;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.TradesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final ReviewRepository reviewRepository;
	private final TradesRepository tradesRepository;
	private final CurrentUserService currentUserService;

	@Override
	public void createReview(ReviewRequestDto reviewRequestDto) {
		Users writer = currentUserService.getCurrentUser();
		Trades trade = tradesRepository.findById(reviewRequestDto.getTradesId())
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 해당 거래가 COMPLETED 되었는지 검증.
		if (!trade.getStatus().equals(TradeStatus.COMPLETED)) {
			throw new CustomException(ErrorCode.REVIEW_TRADE_NOT_COMPLETED);
		}

		// 리뷰 대상자 설정
		Users reviewee;
		if (trade.getRequester().getUsersId().equals(writer.getUsersId())) {
			reviewee = trade.getOwner(); // 요청자가 작성하는 경우 -> 리뷰 대상자는 소유자
		} else if (trade.getOwner().getUsersId().equals(writer.getUsersId())) {
			reviewee = trade.getRequester(); // 소유자가 작성하는 경우 -> 리뷰 대상자는 요청자
		} else {
			throw new CustomException(ErrorCode.REVIEW_UNAUTHORIZED_ACCESS);
		}

		// 중복 리뷰 확인
		boolean existingReview = reviewRepository.existsByTradeAndWriter(trade, writer);
		if (existingReview) {
			throw new CustomException(ErrorCode.REVIEW_DUPLICATE);
		}

		// 리뷰 저장
		Reviews review = Reviews.builder()
			.writer(writer)
			.reviewee(reviewee)
			.trade(trade)
			.content(reviewRequestDto.getContent())
			.rating(reviewRequestDto.getRating())
			.build();

		reviewRepository.save(review);
	}

	@Override
	public ReviewListDto getMyReviews() {
		Users reviewee = currentUserService.getCurrentUser();
		List<ReviewDto> reviewDtos = reviewRepository.findAllByRevieweeOrderByCreatedAtDesc(reviewee)
			.stream()
			.map(ReviewDto::of)
			.toList();

		return new ReviewListDto(reviewDtos.size(), reviewDtos);
	}
}