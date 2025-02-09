package com.example.swapit.repository;

import java.time.LocalDateTime;
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

	/**
	 * 동적 쿼리로 size+1 만큼의 Goods 목록 조회
	 *
	 * @param cursorValue 커서 기준 필드 값 (조회수, 생성일, 가격 등)
	 * @param cursorId 마지막 물건 ID
	 * @param categoryIds 조회 카테고리 리스트
	 * @param keyword 검색어
	 * @param sortBy 정렬기준
	 * @param size 조회 개수
	 * @return size+1개의 Goods 목록
	 */
	@Override
	public List<Goods> findGoodsByCursor(
		Long cursorValue, Long cursorId, LocalDateTime createdAt, List<Long> categoryIds, String keyword, String sortBy,
		int size
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

		// 동적 조건: 커서 기반 페이지네이션 (cursorFieldValue + goodsId)
		BooleanExpression cursorCondition = (cursorId == null)
			? null // 처음 요청일 경우 커서 조건 제외
			: switch (safeSortby) {
			// 인기순 (조회수 내림차순)
			// WHERE view_count < cursorFieldValue
			// OR (view_count = cursorFieldValue AND goods_id > goodsId)
			case "popular" -> (cursorValue == null) ? null
				: qGoods.viewCount.lt(cursorValue)
				.or(qGoods.viewCount.eq(cursorValue).and(qGoods.id.gt(cursorId)));

			// 최신순 (생성일 내림차순)
			// WHERE created_at < cursorCreatedAt
			// OR (created_at = cursorCreatedAt AND goods_id < cursorId)
			case "recent" -> (createdAt == null) ? null
				: qGoods.createdAt.lt(createdAt) // 현재 커서 날짜 이전 데이터만 가져옴
				.or(qGoods.createdAt.eq(createdAt).and(qGoods.id.lt(cursorId))); // 같은 날짜면 id가 작은 것만 가져옴

			// 높은 가격순 (가격 내림차순)
			// WHERE price < cursorFieldValue
			// OR (price = cursorFieldValue AND goods_id > goodsId)
			case "priceHigh" -> (cursorValue == null) ? null
				: qGoods.price.lt(cursorValue)
				.or(qGoods.price.eq(cursorValue).and(qGoods.id.gt(cursorId)));

			// 낮은 가격순 (가격 오름차순)
			// WHERE price > cursorFieldValue
			// OR (price = cursorFieldValue AND goods_id > goodsId)
			case "priceLow" -> (cursorValue == null) ? null
				: qGoods.price.gt(cursorValue) // 가격이 cursorValue 초과 (>)
				.or(qGoods.price.eq(cursorValue).and(qGoods.id.gt(cursorId))); // 같은 가격이면 id가 cursorId보다 큰 것 (>)

			// 기본값 : 인기순
			default -> (cursorValue == null) ? null
				: qGoods.viewCount.lt(cursorValue)
				.or(qGoods.viewCount.eq(cursorValue).and(qGoods.id.gt(cursorId)));
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