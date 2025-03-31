package com.example.swapit.service;

import static org.assertj.core.api.Assertions.*;
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
import org.springframework.dao.DataIntegrityViolationException;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Reviews;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.ReviewListDto;
import com.example.swapit.domain.dto.ReviewRequestDto;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.trade.TradesRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@InjectMocks
	private ReviewServiceImpl reviewService;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private CurrentUserService currentUserService;

	private Users writer;
	private Users owner;
	private Goods requestedGoods;
	private Goods targetGoods;
	private Trades completedTrade;
	private Trades inProgressTrade;
	private ReviewRequestDto reviewRequestDto;
	private Reviews review;

	@BeforeEach
	void setUp() {
		writer = Users.builder().usersId(1L).profileImageUrl("image").build();
		owner = Users.builder().usersId(2L).build();

		requestedGoods = Goods.builder().user(writer).build();
		targetGoods = Goods.builder().user(owner).build();

		completedTrade = Trades.builder()
			.id(1L)
			.requestedGoods(requestedGoods)
			.targetGoods(targetGoods)
			.status(TradeStatus.COMPLETED) // 거래 완료 상태
			.build();

		inProgressTrade = Trades.builder()
			.id(2L)
			.requestedGoods(requestedGoods)
			.targetGoods(targetGoods)
			.status(TradeStatus.INPROGRESS) // 거래 진행 중 상태
			.build();

		reviewRequestDto = new ReviewRequestDto(1L, 4.5, "좋은 거래였습니다!");

		review = Reviews.builder()
			.writer(writer)
			.reviewee(owner)
			.trade(completedTrade)
			.content(reviewRequestDto.getContent())
			.rating(reviewRequestDto.getRating())
			.build();
	}

	@Test
	@DisplayName("리뷰 생성 성공 테스트")
	void createReview_Success() {
		// given
		when(currentUserService.getCurrentUser()).thenReturn(writer);
		when(tradesRepository.findById(1L)).thenReturn(Optional.of(completedTrade));

		// when
		reviewService.createReview(reviewRequestDto);

		// then
		verify(reviewRepository, times(1)).save(any(Reviews.class));
	}

	@Test
	@DisplayName("거래가 존재하지 않을 때 예외 발생")
	void createReview_TradeNotFound() {
		// given
		when(currentUserService.getCurrentUser()).thenReturn(writer);
		when(tradesRepository.findById(1L)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(reviewRequestDto))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.TRADES_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("거래가 완료되지 않았을 때 리뷰 작성 불가")
	void createReview_TradeNotCompleted() {
		// given
		ReviewRequestDto reviewRequestDto2 = new ReviewRequestDto(2L, 4.5, "좋은 거래였습니다!");
		when(currentUserService.getCurrentUser()).thenReturn(writer);
		doReturn(Optional.of(inProgressTrade)).when(tradesRepository).findById(2L);

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(reviewRequestDto2))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.REVIEW_TRADE_NOT_COMPLETED.getMessage());
	}

	@Test
	@DisplayName("중복 리뷰 작성 시 예외 발생")
	void createReview_DuplicateReview() {
		// given
		when(currentUserService.getCurrentUser()).thenReturn(writer);
		when(tradesRepository.findById(1L)).thenReturn(Optional.of(completedTrade));

		// 중복 리뷰 가정
		doThrow(DataIntegrityViolationException.class).when(reviewRepository).save(any(Reviews.class));

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(reviewRequestDto))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.REVIEW_DUPLICATE.getMessage());
	}

	@Test
	@DisplayName("리뷰 조회 테스트")
	void getMyReviews_Success() {
		// given
		Reviews review2 = Reviews.builder()
			.writer(writer)
			.reviewee(owner)
			.trade(completedTrade)
			.content("정말 친절한 거래자였어요.")
			.rating(5.0)
			.build();

		when(currentUserService.getCurrentUser()).thenReturn(owner);
		when(reviewRepository.findAllByRevieweeOrderByCreatedAtDesc(owner)).thenReturn(List.of(review, review2));

		// when
		ReviewListDto reviewListDto = reviewService.getMyReviews();

		// then
		assertThat(reviewListDto).isNotNull();
		assertThat(reviewListDto.getReviewCount()).isEqualTo(2);
		assertThat(reviewListDto.getReviewList()).hasSize(2);
		assertThat(reviewListDto.getReviewList().get(0).getContent()).isEqualTo("좋은 거래였습니다!");
	}
}