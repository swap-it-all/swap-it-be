package com.example.swapit.domain.dto.trade;

import com.example.swapit.domain.Goods;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestGoodsImageDto {
	private Goods requestedGoods;
	private Goods myGoods;
}
