package com.example.swapit.domain.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReadReceiptRequestDto {
	private Long lastReadChatId;
}
