package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.dto.trade.RequestGoodsImageDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;

import io.lettuce.core.dynamic.annotation.Param;

public interface TradesRepository extends JpaRepository<Trades, Long> {
	long countByTargetGoodsIdAndIsDeletedFalse(Long goodsId);

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
		+ "WHERE t.requester.id = :userId")
	List<RequestGoodsImageDto> findMyRequests(@Param("userId") Long userId);

	@Query("SELECT t.requestedGoods FROM Trades t WHERE t.targetGoods.id = :goodsId")
	List<Goods> findGoodsRequests(@Param("goodsId") Long goodsId);

}