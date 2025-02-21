package com.example.swapit.domain.dto;

import java.time.LocalDateTime;

import com.example.swapit.domain.Reviews;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class ReviewDto {

	private Long usersId;
	private String nickname;
	private String profileImageUrl;
	private double rating;
	private String content;
	private LocalDateTime createdAt;

	public static ReviewDto of(Reviews review) {
		return ReviewDto.builder()
			.usersId(review.getWriter().getUsersId())
			.nickname(review.getWriter().getNickname())
			.profileImageUrl(review.getWriter().getProfileImageUrl())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.build();
	}
}