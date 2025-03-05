package com.example.swapit.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomAddRequestFromTradeDto {
	@NotNull(message = "tradesId는 필수 입력 값입니다.")
	private Long tradesId;
}