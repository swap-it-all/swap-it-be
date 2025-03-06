package com.example.swapit.domain.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomListResponseDto {
	private List<ChatRoomResponseDto> chatRoomList;
}
