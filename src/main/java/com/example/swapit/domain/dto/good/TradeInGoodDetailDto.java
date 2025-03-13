package com.example.swapit.domain.dto.good;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeInGoodDetailDto {
	private Long tradesId;
	private boolean isRequester;
	private String status;
	private Long relatedGoodsId;

	@JsonProperty("isRequester")
	public boolean getIsRequester() {
		return isRequester;
	}
}
