package com.example.swapit.service;

import com.example.swapit.domain.dto.ReviewListDto;
import com.example.swapit.domain.dto.ReviewRequestDto;

public interface ReviewService {
	void createReview(ReviewRequestDto reviewRequestDto);

	ReviewListDto getMyReviews();
}