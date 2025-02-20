package com.example.swapit.domain.dto;

import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsRequestDto {

	@NotBlank(message = "상품 제목은 필수 입력 값입니다.")
	private String title;

	@NotNull(message = "가격은 필수 입력 값입니다.")
	@Positive(message = "가격은 0보다 커야 합니다.")
	private Long price;

	@NotBlank(message = "상품 상태(quality)는 필수 입력 값입니다.")
	private String quality;

	@NotNull(message = "카테고리는 필수 입력 값입니다.")
	private Long categoryId;

	private String placeName;

	@NotBlank(message = "상품 설명(content)은 필수 입력 값입니다.")
	private String content;

	public Goods toEntity(Users user, Categories category) {
		return Goods.builder()
			.user(user)
			.title(title)
			.price(price)
			.quality(GoodsQuality.valueOf(quality.toUpperCase()))
			.category(category)
			.placeName(placeName)
			.content(content)
			.build();
	}
}