package com.example.swapit.domain.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record ConflictTradeResult(Long tradesId, ConflictType type) {
	@Getter
	@AllArgsConstructor
	public enum ConflictType {
		TRADE_ALREADY_REQUESTED_IN_REVERSE("상대방이 이미 역방향으로 거래 요청한 상태입니다."),
		TRADE_ALREADY_EXISTS_WITH_SAME_GOODS("동일한 거래 조합이 이미 존재합니다."),
		TRADE_ALREADY_REJECTED("이전에 동일한 거래 요청이 거절된 이력이 있습니다."),
		TRADE_REQUESTED_BY_SAME_USER("이미 해당 사용자가 다른 물건으로 거래 요청했습니다.");
		private final String description;
	}
}
