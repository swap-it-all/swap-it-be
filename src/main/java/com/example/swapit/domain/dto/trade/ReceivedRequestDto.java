package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ReceivedRequestDto extends BaseTradeGoodsDto {
	private final String photoUrl;
	private final LocalDateTime createdAt;

	public ReceivedRequestDto(Long id, String title, long price, String category, String placeName, String photoUrl,
		LocalDateTime createdAt) {
		super(id, title, price, category, placeName);
		this.photoUrl = photoUrl;
		this.createdAt = createdAt;
	}
}
