package com.example.swapit.repository.trade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.TradeGoodsDao;
import com.example.swapit.domain.dto.trade.InProgressCountDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;

import io.lettuce.core.dynamic.annotation.Param;

public interface TradesRepository extends JpaRepository<Trades, Long> {
	long countByTargetGoodsIdAndStatus(Long goodsId, TradeStatus status);

	@Modifying
	@Query("UPDATE Trades t SET t.status = 'REJECTED' WHERE t.targetGoods = :targetGoods AND t.id <> :acceptedTradeId")
	void rejectOtherTrades(@Param("targetGoods") Goods targetGoods, @Param("acceptedTradeId") Long acceptedTradeId);

	@Query("SELECT t.targetGoods.id AS goodsId, COALESCE(COUNT(t), 0) AS tradeCount "
		+ "FROM Trades t "
		+ "WHERE t.targetGoods.id IN :goodsIds AND t.status <> 'REJECTED' "
		+ "GROUP BY t.targetGoods.id")
	List<TradeCountProjection> findTradeCountByGoodsIds(@Param("goodsIds") List<Long> goodsIds);

	@Query("SELECT new com.example.swapit.domain.dao.TradeGoodsDao(t.targetGoods, t.requestedGoods, t.id) "
		+ "FROM Trades t "
		+ "WHERE t.requestedGoods.user.usersId = :userId AND t.status <> 'REJECTED'")
	List<TradeGoodsDao> findMyRequests(@Param("userId") Long userId);

	@Query("SELECT t.requestedGoods FROM Trades t WHERE t.targetGoods.id = :goodsId AND t.status <> 'REJECTED' ORDER BY t.createdAt DESC")
	List<Goods> findGoodsRequests(@Param("goodsId") Long goodsId);

	@Query("SELECT new com.example.swapit.domain.dto.trade.InProgressCountDto(t.targetGoods.id, COUNT(t)) "
		+ "FROM Trades t "
		+ "WHERE t.targetGoods.id IN :goodsIds AND t.status = 'INPROGRESS' "
		+ "GROUP BY t.targetGoods.id")
	List<InProgressCountDto> findInProgressCountByGoodsIds(@Param("goodsIds") List<Long> goodsIds);

	@Query("SELECT t.id FROM Trades t WHERE t.requestedGoods = :requested AND t.targetGoods = :target AND t.status <> :status")
	Optional<Long> findIdByRequestedGoodsAndTargetGoodsAndStatusNot(@Param("requested") Goods requested,
		@Param("target") Goods target, @Param("status") TradeStatus status);

	boolean existsByRequestedGoodsAndTargetGoodsAndStatus(Goods requestedGoods, Goods TargetGoods, TradeStatus status);

	@Query("""
		SELECT t FROM Trades t
		WHERE (t.targetGoods.id = :goodsId AND t.requestedGoods.user.usersId = :userId)
		OR (t.requestedGoods.id = :goodsId AND t.targetGoods.user.usersId = :userId)
		ORDER BY t.updatedAt DESC""")
	Optional<Trades> findUserRelatedTrade(@Param("goodsId") Long goodsId, @Param("userId") Long userId);

	@Query("SELECT t.id FROM Trades t WHERE t.targetGoods = :target AND t.requestedGoods.user = :requestUser AND t.status <> :status")
	Optional<Long> findIdByTargetGoodsAndRequestedGoodsUserAndStatusNot(@Param("target") Goods target,
		@Param("requestUser") Users requestUser, @Param("status") TradeStatus status);

	@Query("""
		SELECT t FROM Trades t
		WHERE (t.requestedGoods.user.usersId = :userId)
		OR (t.targetGoods.user.usersId = :userId)
		""")
	List<Trades> findAllByUser(@Param("userId") Long userId);
}
