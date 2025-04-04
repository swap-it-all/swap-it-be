package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseTradeGoodsDto {
	protected Long goodsId;
	protected String title;
	protected long price;
	protected String category;
	protected String placeName;
	protected boolean isInProgress;
	protected LocalDateTime createdAt;

	@JsonProperty("isInProgress")
	public boolean getIsInProgress() {
		return isInProgress;
	}
}
