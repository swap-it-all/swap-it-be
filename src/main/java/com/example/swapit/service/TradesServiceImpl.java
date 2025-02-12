package com.example.swapit.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradesServiceImpl implements TradesService {

	private final TradesRepository tradesRepository;
	private final GoodsRepository goodsRepository;

	@Override
	public void requestTrade(TradesRequestDto tradesRequestDto) {
		Goods requestedGoods = goodsRepository.findById(tradesRequestDto.getRequestedGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Goods targetGoods = goodsRepository.findById(tradesRequestDto.getTargetGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		long count = tradesRepository.countByTargetGoodsIdAndIsDeletedFalse(tradesRequestDto.getTargetGoodsId());

		if (count >= 10) {
			throw new CustomException(ErrorCode.MAXIMUM_TRADE_REQUEST);
		}

		try {
			tradesRepository.save(new Trades(requestedGoods, targetGoods));
		} catch (DataIntegrityViolationException ex) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}
	}

	@Override
	public void cancelTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));
		tradesRepository.delete(trades);
	}
}
