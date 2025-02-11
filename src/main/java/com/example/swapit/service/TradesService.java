package com.example.swapit.service;

import com.example.swapit.domain.dto.TradesRequestDto;

public interface TradesService {
	void requestTrade(TradesRequestDto tradesRequestDto);

	void cancelTrade(Long tradesId);
}
