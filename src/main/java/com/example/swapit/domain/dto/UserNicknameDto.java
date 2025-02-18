package com.example.swapit.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserNicknameDto {
	@NotBlank(message = "닉네임은 필수 입력 값입니다.")
	private String nickname;
}
