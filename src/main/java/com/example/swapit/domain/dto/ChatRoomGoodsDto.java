package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomGoodsDto {
	private Long goodsId;
	private String title;
	private String category;
	private long price;
	private String imageUrl;
}
