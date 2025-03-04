package com.example.swapit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.trade.RequestGoodsImageDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;

import io.lettuce.core.dynamic.annotation.Param;

public interface TradesRepository extends JpaRepository<Trades, Long> {
	long countByTargetGoodsIdAndStatus(Long goodsId, TradeStatus status);

	@Query("SELECT COUNT(*) FROM Trades t WHERE ( t.targetGoods.user = :user OR t.requestedGoods.user = :user ) AND t.status = 'COMPLETED'")
	long countByCompletedTradesByUser(@Param("user") Users user);

	@Modifying
	@Query("UPDATE Trades t SET t.status = 'REJECTED' WHERE t.targetGoods = :targetGoods AND t.id <> :acceptedTradeId")
	void rejectOtherTrades(@Param("targetGoods") Goods targetGoods, @Param("acceptedTradeId") Long acceptedTradeId);

	@Query("SELECT t.targetGoods.id AS goodsId, COALESCE(COUNT(t), 0) AS tradeCount "
		+ "FROM Trades t "
		+ "WHERE t.targetGoods.id IN :goodsIds "
		+ "GROUP BY t.targetGoods.id")
	List<TradeCountProjection> findTradeCountByGoodsIds(@Param("goodsIds") List<Long> goodsIds);

	@Query("SELECT new com.example.swapit.domain.dto.trade.RequestGoodsImageDto(t.targetGoods, t.requestedGoods) "
		+ "FROM Trades t "
		+ "WHERE t.requestedGoods.user.id = :userId")
	List<RequestGoodsImageDto> findMyRequests(@Param("userId") Long userId);

	@Query("SELECT t.requestedGoods FROM Trades t WHERE t.targetGoods.id = :goodsId")
	List<Goods> findGoodsRequests(@Param("goodsId") Long goodsId);

	Optional<Trades> findByRequestedGoodsUserAndTargetGoods(Users requester, Goods targetGoods);

	boolean existsByTargetGoodsAndStatusNot(Goods targetGoods, TradeStatus status);
}