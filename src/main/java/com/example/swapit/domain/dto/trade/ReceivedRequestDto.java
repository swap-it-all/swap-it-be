package com.example.swapit.domain.dto.trade;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 스왑 목록 > 내가 받은 스왑 요청 dto
 */
@Getter
public class ReceivedRequestDto extends BaseTradeGoodsDto {
	private final String photoUrl;

	public ReceivedRequestDto(Long id, String title, long price, String category, String placeName, String photoUrl,
		LocalDateTime createdAt) {
		super(id, title, price, category, placeName, createdAt);
		this.photoUrl = photoUrl;
	}
}
