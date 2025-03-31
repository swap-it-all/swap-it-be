package com.example.swapit.repository.trade;

import java.util.Optional;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.ConflictTradeResult;

public interface TradeQueryRepository {
	Optional<ConflictTradeResult> findConflictTrade(Goods requested, Goods target, Users requester);
}
