package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;

public interface GoodsService {

	// 물건 조회
	GoodsListDto getGoods(Long cursorValue, Long cursorId, LocalDateTime createdAt, List<Long> categoryIds,
		String keyword, String sortBy);

	List<GoodsDto> getMyGoods();

	GoodsDetailDto getGoodDetail(Long goodsId);

	void insertGood(GoodsRequestDto goodsRequestDto);

	void updateGood(Long goodsId, GoodsRequestDto goodsRequestDto);

	void deleteGood(Long goodsId);
}