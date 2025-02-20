package com.example.swapit.service;

import java.util.List;

import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.ReviewRequestDto;

public interface ReviewService {
	void createReview(ReviewRequestDto reviewRequestDto);

	List<ReviewDto> getMyReviews();
}