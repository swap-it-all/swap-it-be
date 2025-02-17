package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequesterGoodsDto {
	private Long goodsId;
	private String title;
	private String requesterNickname;
}
