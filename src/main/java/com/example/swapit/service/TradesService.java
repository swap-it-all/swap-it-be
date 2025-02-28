package com.example.swapit.service;

import java.util.List;

import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.domain.dto.trade.TradesRequestDto;

public interface TradesService {
	void requestTrade(TradesRequestDto tradesRequestDto);

	void cancelTrade(Long tradesId);

	void acceptTrade(Long tradesId);

	void rejectTrade(Long tradesId);

	void completeTrade(Long tradesId);

	List<MyGoodsDto> getMyGoods();

	List<ReceivedRequestDto> getGoodsRequests(Long goodsId);

	List<MyRequestDto> getMyRequests();
}