package com.example.swapit.domain.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomAddRequestFromGoodDto {
	@NotNull(message = "goodsId는 필수 입력 값입니다.")
	private Long goodsId;
}