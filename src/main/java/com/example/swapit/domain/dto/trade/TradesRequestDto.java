package com.example.swapit.domain.dto.trade;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradesRequestDto {
	@NotNull(message = "requestedGoodsId는 필수 입력 값입니다.")
	private Long requestedGoodsId;

	@NotNull(message = "targetGoodsId는 필수 입력 값입니다.")
	private Long targetGoodsId;
}