package com.example.swapit.domain.dto;

import com.example.swapit.domain.ChatType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatStompRequestDto {
	private ChatType chatType;
	private String content;
	private Long goodsId;
}
