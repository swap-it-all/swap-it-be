package com.example.swapit.domain.dto.good;

import java.time.LocalDateTime;

import com.example.swapit.domain.Goods;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class GoodsDto {

	private Long goodsId;
	private String title;
	private long price;
	private String category;
	private String goodsTradeStatus;
	private String imageUrl;
	private String placeName;
	private long viewCount;
	private LocalDateTime createdAt;

	public static GoodsDto of(Goods good, String imageUrl) {
		return GoodsDto.builder()
			.goodsId(good.getId())
			.title(good.getTitle())
			.price(good.getPrice())
			.category(good.getCategory().getName())
			.goodsTradeStatus(good.getGoodsTradeStatus().name())
			.imageUrl(imageUrl)
			.placeName(good.getPlaceName())
			.viewCount(good.getViewCount())
			.createdAt(good.getCreatedAt())
			.build();
	}
}