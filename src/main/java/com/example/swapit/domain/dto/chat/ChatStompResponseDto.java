package com.example.swapit.domain.dto.chat;

import java.time.LocalDateTime;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;
import com.example.swapit.domain.dto.RequesterGoodsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatStompResponseDto {
	private Long chatsId;
	private Long senderId;
	private ChatType chatType;
	private String content;
	private RequesterGoodsDto requesterGoods;
	private LocalDateTime createdAt;

	public ChatStompResponseDto(Chats chats, RequesterGoodsDto requesterGoods) {
		this.chatsId = chats.getId();
		this.senderId = chats.getSender().getUsersId();
		this.chatType = chats.getChatType();
		this.content = chats.getContent();
		this.requesterGoods = requesterGoods;
		this.createdAt = chats.getCreatedAt();
	}
}
