package com.example.swapit.repository.trade;

import java.util.Optional;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.QTrades;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.ConflictTradeResult;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TradeQueryRepositoryImpl implements TradeQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<ConflictTradeResult> findConflictTrade(Goods requested, Goods target, Users requester) {
		QTrades t = QTrades.trades;

		// 조건 별로 case 분기 : select 포함되는 표현 식.
		NumberTemplate<Integer> conflictType = (NumberTemplate<Integer>)new CaseBuilder()
			.when(t.requestedGoods.eq(target)
				.and(t.targetGoods.eq(requested))
				.and(t.status.ne(TradeStatus.REJECTED))
				.and(t.isDeleted.isFalse()))
			.then(0) // TRADE_ALREADY_REQUESTED_IN_REVERSE

			.when(t.requestedGoods.eq(requested)
				.and(t.targetGoods.eq(target))
				.and(t.isDeleted.isFalse()))
			.then(1) // TRADE_ALREADY_EXISTS_WITH_SAME_GOODS

			.when(t.requestedGoods.eq(requested)
				.and(t.targetGoods.eq(target))
				.and(t.status.eq(TradeStatus.REJECTED))
				.and(t.isDeleted.isFalse()))
			.then(2) // TRADE_ALREADY_REJECTED

			.when(t.targetGoods.eq(target)
				.and(t.requestedGoods.user.eq(requester))
				.and(t.status.ne(TradeStatus.REJECTED))
				.and(t.isDeleted.isFalse()))
			.then(3) // TRADE_REQUESTED_BY_SAME_USER

			.otherwise(-1);

		// 조회 쿼리
		Tuple result = queryFactory
			.select(t.id, conflictType)
			.from(t)
			.where(
				t.isDeleted.isFalse().and(
					// 1. 역방향 거래 요청
					t.requestedGoods.eq(target).and(t.targetGoods.eq(requested)).and(t.status.ne(TradeStatus.REJECTED))
						// 2. 동일한 거래 조합이 이미 존재
						.or(t.requestedGoods.eq(requested)
							.and(t.targetGoods.eq(target))
							.and(t.status.ne(TradeStatus.REJECTED)))
						// 3. 동일한 거래가 과거에 거절된 적 있음
						.or(t.requestedGoods.eq(requested)
							.and(t.targetGoods.eq(target))
							.and(t.status.eq(TradeStatus.REJECTED)))
						// 4. 동일 사용자가 이미 다른 물건으로 요청한 상태
						.or(t.targetGoods.eq(target)
							.and(t.requestedGoods.user.eq(requester))
							.and(t.status.ne(TradeStatus.REJECTED)))
				)
			)
			.limit(1)
			.fetchOne();

		if (result == null)
			return Optional.empty();

		Long tradeId = result.get(t.id);
		Integer typeIndex = result.get(conflictType);

		// conflictType 인덱스를 ConflictTradeResult enum으로 매핑
		ConflictTradeResult.ConflictType type = switch (typeIndex) {
			case 0 -> ConflictTradeResult.ConflictType.TRADE_ALREADY_REQUESTED_IN_REVERSE;
			case 1 -> ConflictTradeResult.ConflictType.TRADE_ALREADY_EXISTS_WITH_SAME_GOODS;
			case 2 -> ConflictTradeResult.ConflictType.TRADE_ALREADY_REJECTED;
			case 3 -> ConflictTradeResult.ConflictType.TRADE_REQUESTED_BY_SAME_USER;
			default -> {
				log.error("[거래 생성 오류 (매핑된 enum) 타입 없음] : tradeId {}", tradeId);
				throw new IllegalStateException("거래 생성 오류 (매핑된 enum) 타입 없음: " + typeIndex);
			}
		};

		return Optional.of(new ConflictTradeResult(tradeId, type));
	}
}
