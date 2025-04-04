package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 스왑 목록 > 내가 요청한 스왑 요청 dto
 */
@Getter
public class MyRequestDto extends BaseTradeGoodsDto {
	private final String myGoodsPhotoUrl;
	private final String targetGoodsPhotoUrl;
	private final long targetGoodsViewCount;
	private final Long tradesId;

	public MyRequestDto(Long id, String title, long price, String category, String placeName, String myGoodsPhotoUrl,
		String targetGoodsPhotoUrl, long targetGoodsViewCount, boolean isInProgress, LocalDateTime createdAt,
		Long tradesId) {
		super(id, title, price, category, placeName, isInProgress, createdAt);
		this.myGoodsPhotoUrl = myGoodsPhotoUrl;
		this.targetGoodsPhotoUrl = targetGoodsPhotoUrl;
		this.targetGoodsViewCount = targetGoodsViewCount;
		this.tradesId = tradesId;
	}
}
