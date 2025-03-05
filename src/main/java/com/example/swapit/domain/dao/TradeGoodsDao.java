package com.example.swapit.domain.dao;

import com.example.swapit.domain.Goods;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TradeGoodsDao {
	private Goods targetGoods;
	private Goods myGoods;
}
