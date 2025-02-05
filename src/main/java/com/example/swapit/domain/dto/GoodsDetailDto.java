package com.example.swapit.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;

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
	private List<String> imageUrls;
	private LocalDateTime createdAt;

	public static GoodsDetailDto of(Goods good) {
		return GoodsDetailDto.builder()
			.goodsId(good.getId())
			.user(UserProfileDto.of(good.getUser()))
			.category(good.getCategory().getName())
			.title(good.getTitle())
			.price(good.getPrice())
			.quality(good.getQuality().name())
			.content(good.getContent())
			.goodsTradeStatus(good.getGoodsTradeStatus().name())
			.placeName(good.getPlaceName())
			.viewCount(good.getViewCount())
			// todo: 성능 개선 필요.
			.imageUrls(
				good.getGoodsImagesList()
					.stream()
					.map(GoodsImages::getImageUrl)
					.collect(Collectors.toList())
			)
			.createdAt(good.getCreatedAt())
			.build();
	}
}
