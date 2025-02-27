package com.example.swapit.domain.dto;

import com.example.swapit.domain.Users;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class UserProfileDto {
	private Long userId;
	private String nickname;
	private String profileImageUrl;
	private Double userRating;

	public static UserProfileDto of(Users user, Double averageRating) {
		return UserProfileDto.builder()
			.userId(user.getUsersId())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImageUrl())
			.userRating(averageRating)
			.build();
	}
}