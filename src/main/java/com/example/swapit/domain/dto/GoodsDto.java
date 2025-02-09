package com.example.swapit.domain.dto;

import java.time.LocalDateTime;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;

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
	private String photoUrl;
	private String placeName;
	private long viewCount;
	private LocalDateTime createdAt;

	public static GoodsDto of(Goods good) {
		return GoodsDto.builder()
			.goodsId(good.getId())
			.title(good.getTitle())
			.price(good.getPrice())
			.category(good.getCategory().getName())
			.photoUrl(
				good.getGoodsImagesList().stream()
					.findFirst()
					.map(GoodsImages::getImageUrl)
					.orElseGet(() -> "/images/goods/default"))
			.placeName(good.getPlaceName())
			.viewCount(good.getViewCount())
			.createdAt(good.getCreatedAt())
			.build();
	}
}