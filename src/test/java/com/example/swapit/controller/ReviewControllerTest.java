package com.example.swapit.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.ReviewListDto;
import com.example.swapit.domain.dto.ReviewRequestDto;
import com.example.swapit.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

	private MockMvc mockMvc;

	@Mock
	private ReviewService reviewService;

	@InjectMocks
	private ReviewController reviewController;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("리뷰 작성 성공 테스트")
	void writeReview_Success() throws Exception {
		// Given
		ReviewRequestDto reviewRequestDto = new ReviewRequestDto(1L, 4.5, "좋은 거래였습니다!");
		doNothing().when(reviewService).createReview(any(ReviewRequestDto.class));

		// When & Then
		mockMvc.perform(post("/api/user/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reviewRequestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 유효하지 않은 데이터")
	void writeReview_InvalidData() throws Exception {
		// Given: tradesId가 null이면 @Valid에 의해 실패해야 함
		ReviewRequestDto invalidRequest = new ReviewRequestDto(null, 4.5, "좋은 거래였습니다!");

		// When & Then
		mockMvc.perform(post("/api/user/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("내 리뷰 목록 조회 테스트")
	void getMyReviews_Success() throws Exception {
		// Given
		List<ReviewDto> reviewList = List.of(
			new ReviewDto(1L, "writer1", "image", 5.0, "거래자가 친절했어요!", LocalDateTime.now()),
			new ReviewDto(2L, "writer2", "image", 4.5, "좋은 거래였습니다!", LocalDateTime.now())
		);
		ReviewListDto reviewListDto = new ReviewListDto(reviewList.size(), reviewList);

		when(reviewService.getMyReviews()).thenReturn(reviewListDto);

		// When & Then
		mockMvc.perform(get("/api/user/reviews/my"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.reviewCount").value(2))
			.andExpect(jsonPath("$.results.reviewList.length()").value(2))
			.andExpect(jsonPath("$.results.reviewList[0].content").value("거래자가 친절했어요!"))
			.andExpect(jsonPath("$.results.reviewList[1].rating").value(4.5));
	}
}