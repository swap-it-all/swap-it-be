package com.example.swapit.repository;

import java.util.List;

import com.example.swapit.domain.Goods;

public interface CustomGoodsRepository {
	List<Goods> findGoodsByCursor(
		Long cursor,                // 마지막으로 조회된 항목 ID
		List<Long> categoryIds,     // 카테고리 ID 리스트
		String keyword,             // 검색어
		String sortBy,              // 정렬 기준
		int size                    // 가져올 데이터 수
	);
}