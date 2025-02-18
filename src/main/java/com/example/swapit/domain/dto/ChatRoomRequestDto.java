package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomRequestDto {
	private Long goodsId;
	private Long requesterId;
}
