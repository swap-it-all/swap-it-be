package com.example.swapit.domain.dto.trade;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeMyGoodsRequestDto {
	String myGoodsTitle;
	private List<ReceivedRequestDto> goodsList;
}
