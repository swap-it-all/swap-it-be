package com.example.swapit.domain.dto;

import java.time.LocalDateTime;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatStompResponseDto {
	private Long chatsId;
	private Long senderId;
	private ChatType chatType;
	private String content;
	private LocalDateTime createdAt;

	public ChatStompResponseDto(Chats chats) {
		this.chatsId = chats.getId();
		this.senderId = chats.getSender().getUsersId();
		this.chatType = chats.getChatType();
		this.content = chats.getContent();
		this.createdAt = chats.getCreatedAt();
	}
}
