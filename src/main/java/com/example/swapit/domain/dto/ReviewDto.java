package com.example.swapit.domain.dto;

import com.example.swapit.domain.Reviews;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class ReviewDto {

	private Long writerId;
	private String writerNickname;
	private String profileImageUrl;
	private double rating;
	private String content;

	public static ReviewDto of(Reviews review) {
		return ReviewDto.builder()
			.writerId(review.getWriter().getUsersId())
			.writerNickname(review.getWriter().getNickname())
			.profileImageUrl(review.getWriter().getProfileImageUrl())
			.rating(review.getRating())
			.content(review.getContent())
			.build();
	}
}