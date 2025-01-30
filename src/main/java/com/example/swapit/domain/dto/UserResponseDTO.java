package com.example.swapit.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDTO {
	private String nickname;
	private String email;
	private String provider;
	private String profileImgUrl;
}
