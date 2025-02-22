package com.example.swapit.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserPageDto {
	private Long usersId;
	private String nickname;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String email;
	private String profileImageUrl;
	private long totalGoodsCount;
	private long completedSwapCount;
	private double ratingAverage;
	private List<ReviewDto> reviews;
}