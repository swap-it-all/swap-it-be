package com.example.swapit.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatListDto {
	private List<ChatDto> chatList;
	private boolean hasNext;
	private Long lastCursorId;
	private int size;
}
