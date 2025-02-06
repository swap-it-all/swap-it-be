package com.example.swapit.domain.dto;

import java.util.List;

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

	@NotBlank(message = "카테고리는 필수 입력 값입니다.")
	private Long categoryId;

	@NotBlank(message = "거래 장소(placeName)는 필수 입력 값입니다.")
	private String placeName;

	@NotBlank(message = "상품 설명(content)은 필수 입력 값입니다.")
	private String content;

	// todo: 사진은 "필수"로 처리할지, 상의 필요.
	private List<String> photoUrls;

	public Goods toEntity(Users user, Categories category) {
		// todo: 사진 api 추가 후, photoUrls 연결 추가.
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
