package com.example.swapit.domain.dto.trade;

import lombok.Getter;

@Getter
public class MyRequestDto extends BaseTradeGoodsDto {
	private final String myGoodsPhotoUrl;
	private final String requestedGoodsPhotoUrl;

	public MyRequestDto(Long id, String title, long price, String category, String placeName, String myGoodsPhotoUrl,
		String requestedGoodsPhotoUrl) {
		super(id, title, price, category, placeName);
		this.myGoodsPhotoUrl = myGoodsPhotoUrl;
		this.requestedGoodsPhotoUrl = requestedGoodsPhotoUrl;
	}
}
