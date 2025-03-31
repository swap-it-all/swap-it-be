package com.example.swapit.repository.good;

import java.time.LocalDateTime;
import java.util.List;

import com.example.swapit.domain.Goods;

public interface CustomGoodsRepository {
	List<Goods> findGoodsByCursor(
		Long cursorValue,           // 마지막 커서 값 (price, viewCount 등등..)
		Long cursorId,              // 마지막으로 조회된 물건 ID
		LocalDateTime createdAt,    // 생성일시
		List<Long> categoryIds,     // 카테고리 ID 리스트
		String keyword,             // 검색어
		String sortBy,              // 정렬 기준
		int size                    // 가져올 데이터 수
	);
}