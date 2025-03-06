package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.dto.Dto;
import com.example.swapit.domain.dto.good.GoodsDetailDto;
import com.example.swapit.domain.dto.good.GoodsListDto;
import com.example.swapit.domain.dto.good.GoodsRequestDto;
import com.example.swapit.domain.dto.good.MyGoodDto;

public interface GoodsService {

	// 물건 조회
	GoodsListDto getGoods(Long cursorValue, Long cursorId, LocalDateTime createdAt, List<Long> categoryIds,
		String keyword, String sortBy);

	Dto<List<MyGoodDto>> getMyGoods(String goodTradeStatus);

	GoodsDetailDto getGoodDetail(Long goodsId);

	Long insertGood(GoodsRequestDto goodsRequestDto);

	void updateGood(Long goodsId, GoodsRequestDto goodsRequestDto);

	void deleteGood(Long goodsId);

	// 물건 사진
	void uploadGoodImages(Long goodsId, List<MultipartFile> images);

	void deleteGoodImage(Long goodsId, Long imagesId);
}