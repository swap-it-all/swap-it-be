package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageDto {
	private Long senderId;
	private String messageType;
	private String content;
}
