package com.example.swapit.domain.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InProgressCountDto {
	private Long goodsId;
	private Long inProgressCount;
}
