package com.example.swapit.service;

import org.springframework.stereotype.Service;

import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;

@Service
public interface GoodsService {

	// 물건 조회
	GoodsListDto getGoods();

	GoodsDetailDto getGoodDetail(Long goodsId);

	void insertGood(GoodsRequestDto goodsRequestDto);

	void updateGood(Long goodsId, GoodsRequestDto goodsRequestDto);

	void deleteGood(Long goodsId);
}
