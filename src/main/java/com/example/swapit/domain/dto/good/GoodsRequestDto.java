package com.example.swapit.domain.dto.good;

import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;

import jakarta.validation.constraints.Max;
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
	@Max(value = 100_000_000, message = "가격은 1억 이하로 입력해야 합니다.")
	private Long price;

	@NotBlank(message = "상품 상태는 필수 입력 값입니다.")
	private String quality;

	@NotNull(message = "카테고리는 필수 입력 값입니다.")
	private Long categoryId;

	@NotBlank(message = "거래 희망 장소는 필수 입력 값입니다.")
	private String placeName;

	@NotBlank(message = "상품 설명은 필수 입력 값입니다.")
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