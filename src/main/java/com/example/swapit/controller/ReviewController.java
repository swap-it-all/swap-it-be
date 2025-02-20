package com.example.swapit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.ReviewDto;
import com.example.swapit.domain.dto.ReviewRequestDto;
import com.example.swapit.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ApiResponse<Void> writeReview(@Valid @RequestBody ReviewRequestDto reviewRequestDto) {
		reviewService.createReview(reviewRequestDto);
		return ApiResponse.success();
	}

	@GetMapping
	public ApiResponse<List<ReviewDto>> getMyReviews() {
		return ApiResponse.success(reviewService.getMyReviews());
	}
}