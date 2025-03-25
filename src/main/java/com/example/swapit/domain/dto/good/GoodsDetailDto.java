package com.example.swapit.domain.dto.good;

import java.time.LocalDateTime;
import java.util.List;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.dto.UserProfileDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class GoodsDetailDto {
	private Long goodsId;
	private UserProfileDto user;
	private String category;
	private String title;
	private long price;
	private String quality;
	private String content;
	private String goodsTradeStatus;
	private String placeName;
	private long viewCount;
	private List<GoodsImageDto> images;
	private TradeInfoDto trade;
	private LocalDateTime createdAt;

	public static GoodsDetailDto of(Goods good, UserProfileDto userProfileDto, List<GoodsImageDto> images,
		TradeInfoDto trade) {
		return GoodsDetailDto.builder()
			.goodsId(good.getId())
			.user(userProfileDto)
			.category(good.getCategory().getName())
			.title(good.getTitle())
			.price(good.getPrice())
			.quality(good.getQuality().name())
			.content(good.getContent())
			.goodsTradeStatus(good.getGoodsTradeStatus().name())
			.placeName(good.getPlaceName())
			.viewCount(good.getViewCount())
			.images(images)
			.trade(trade)
			.createdAt(good.getCreatedAt())
			.build();
	}
}