package com.example.swapit.domain.dto.chat;

import com.example.swapit.domain.dto.good.TradeInfoDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomInfoDto {
	private Long goodsId;
	private String title;
	private String category;
	private long price;
	private String imageUrl;
	private Long usersId;
	private String nickname;
	private TradeInfoDto trade;
}
