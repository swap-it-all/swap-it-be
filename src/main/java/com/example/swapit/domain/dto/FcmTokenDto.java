package com.example.swapit.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenDto {

	@NotBlank(message = "FCM 토큰은 필수 입력 값입니다.")
	private String fcmToken;
}