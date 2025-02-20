package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Trades;

import io.lettuce.core.dynamic.annotation.Param;

public interface TradesRepository extends JpaRepository<Trades, Long> {
	long countByTargetGoodsIdAndIsDeletedFalse(Long goodsId);

	@Modifying
	@Query("UPDATE Trades t SET t.status = 'REJECTED' WHERE t.targetGoods = :targetGoods AND t.id <> :acceptedTradeId")
	void rejectOtherTrades(@Param("targetGoods") Goods targetGoods, @Param("acceptedTradeId") Long acceptedTradeId);
}