package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 스왑 목록 > 요청 들어온 내 물건 dto
 */
@Getter
public class MyGoodsDto extends BaseTradeGoodsDto {
	private final String photoUrl;
	private final long viewCount;
	private final long requestCount;

	public MyGoodsDto(Long id, String title, long price, String category, String placeName, String photoUrl,
		long viewCount, long requestCount, boolean isInProgress, LocalDateTime createdAt) {
		super(id, title, price, category, placeName, isInProgress, createdAt);
		this.photoUrl = photoUrl;
		this.viewCount = viewCount;
		this.requestCount = requestCount;
	}
}
