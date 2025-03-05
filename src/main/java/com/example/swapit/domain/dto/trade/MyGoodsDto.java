package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class MyGoodsDto extends BaseTradeGoodsDto {
	private final String photoUrl;
	private final long viewCount;
	private final long requestCount;
	private final long inProgressCount;
	private final LocalDateTime createdAt;

	public MyGoodsDto(Long id, String title, long price, String category, String placeName, String photoUrl,
		long viewCount, long requestCount, long inProgressCount, LocalDateTime createdAt) {
		super(id, title, price, category, placeName);
		this.photoUrl = photoUrl;
		this.viewCount = viewCount;
		this.requestCount = requestCount;
		this.inProgressCount = inProgressCount;
		this.createdAt = createdAt;
	}
}
