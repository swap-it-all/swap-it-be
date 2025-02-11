package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradesRequestDto {
	private Long requestedGoodsId;
	private Long targetGoodsId;
}
