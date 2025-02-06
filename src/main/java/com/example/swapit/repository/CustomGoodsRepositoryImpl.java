package com.example.swapit.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.QGoods;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomGoodsRepositoryImpl implements CustomGoodsRepository {

	private final JPAQueryFactory queryFactory;
	private final QGoods qGoods = QGoods.goods;

	@Override
	public List<Goods> findGoodsByCursor(
		Long cursor, List<Long> categoryIds, String keyword, String sortBy, int size
	) {
		// 동적 조건: 검색어 (제목 검색만 가능)
		BooleanExpression keywordCondition = (keyword == null || keyword.isEmpty())
			? null
			: qGoods.title.containsIgnoreCase(keyword);

		// 동적 조건: 카테고리
		BooleanExpression categoryCondition = (categoryIds == null || categoryIds.isEmpty())
			? null
			: qGoods.category.id.in(categoryIds);

		// 정렬 기준 설정을 Map으로 관리
		Map<String, OrderSpecifier<?>> orderByMap = Map.of(
			"popular", qGoods.viewCount.desc(),
			"recent", qGoods.createdAt.desc(),
			"priceHigh", qGoods.price.desc(),
			"priceLow", qGoods.price.asc()
		);

		// 정렬 기준이 없을 경우, 기본값 설정
		String safeSortby = (sortBy == null) ? "popular" : sortBy;
		OrderSpecifier<?> orderSpecifier = orderByMap.get(safeSortby);

		// 동적 조건: 커서 (커서 기준 다음 거부터 조회)
		BooleanExpression cursorCondition = (cursor == null)
			? null // 처음 요청일 경우 커서 조건 제외
			: switch (safeSortby) {
			case "popular" -> qGoods.viewCount.lt(cursor); // 인기순: 조회수 기준 커서
			case "recent" -> qGoods.createdAt.lt(
				LocalDateTime.ofInstant(Instant.ofEpochMilli(cursor), ZoneId.systemDefault())
			); // 최신순
			case "priceHigh" -> qGoods.price.lt(cursor);
			case "priceLow" -> qGoods.price.gt(cursor);
			default -> qGoods.id.gt(cursor); // 기본값: 인기순
		};

		// todo : 나중에 쿼리 최적화 시, size+1개 가져오는 거랑 count() 서브쿼리로 다음 데이터가 있는지 확인하는 거 최적화 필요.
		// 쿼리 실행
		return queryFactory.selectFrom(qGoods)
			.where(cursorCondition, keywordCondition, categoryCondition)
			.orderBy(orderSpecifier)
			.limit(size + 1) // size + 1로 다음 페이지 여부 확인
			.fetch();
	}
}