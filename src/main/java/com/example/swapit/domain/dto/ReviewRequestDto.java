package com.example.swapit.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

	@NotNull(message = "거래 ID는 필수 입력 값입니다.")
	private Long tradesId;

	@NotNull(message = "평점은 필수 입력 값입니다.")
	@DecimalMin(value = "0.0", inclusive = true, message = "평점은 0 이상이어야 합니다.")
	@DecimalMax(value = "5.0", inclusive = true, message = "평점은 5 이하여야 합니다.")
	private double rating;

	private String content;
}