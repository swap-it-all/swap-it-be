package com.example.swapit.domain.dto.good;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoodsListDto {
	private List<GoodsDto> goodsList;
	private boolean hasNext;
	private Long lastCursorId;
	private int size;
}