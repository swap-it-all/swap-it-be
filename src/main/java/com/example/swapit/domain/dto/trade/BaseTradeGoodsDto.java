package com.example.swapit.domain.dto.trade;

import lombok.Getter;

@Getter
public class BaseTradeGoodsDto {
	protected Long goodsId;
	protected String title;
	protected long price;
	protected String category;
	protected String placeName;

	public BaseTradeGoodsDto(Long goodsId, String title, long price, String category, String placeName) {
		this.goodsId = goodsId;
		this.title = title;
		this.price = price;
		this.category = category;
		this.placeName = placeName;
	}
}
