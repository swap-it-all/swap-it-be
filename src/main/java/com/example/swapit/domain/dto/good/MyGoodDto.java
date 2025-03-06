package com.example.swapit.domain.dto.good;

import java.time.LocalDateTime;

import com.example.swapit.domain.Goods;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyGoodDto {
	private Long goodsId;
	private String title;
	private long price;
	private String category;
	private String goodTradeStatus;
	private String imageUrl;
	private String placeName;
	private long viewCount;
	private LocalDateTime createdAt;

	public static MyGoodDto of(Goods good, String imageUrl) {
		return new MyGoodDto(
			good.getId(),
			good.getTitle(),
			good.getPrice(),
			good.getCategory().getName(),
			good.getGoodsTradeStatus().name(),
			imageUrl,
			good.getPlaceName(),
			good.getViewCount(),
			good.getCreatedAt()
		);
	}
}
