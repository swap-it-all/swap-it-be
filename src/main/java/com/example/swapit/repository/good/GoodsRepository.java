package com.example.swapit.repository.good;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsTradeStatus;
import com.example.swapit.domain.Users;

public interface GoodsRepository extends JpaRepository<Goods, Long>, CustomGoodsRepository {

	List<Goods> findByUserOrderByCreatedAtDesc(Users user);

	List<Goods> findByUserAndGoodsTradeStatusInOrderByCreatedAtDesc(Users user, List<GoodsTradeStatus> statuses);

	long countByUser(Users user);

	long countByUserAndGoodsTradeStatus(Users user, GoodsTradeStatus goodsTradeStatus);
}