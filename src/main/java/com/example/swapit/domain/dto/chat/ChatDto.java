package com.example.swapit.domain.dto.chat;

import java.time.LocalDateTime;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.dto.RequesterGoodsDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatDto {
	private Long chatsId;
	private ChatType chatType;
	private String content;
	private RequesterGoodsDto requesterGoods;
	private Long senderId;
	private LocalDateTime createdAt;
}
