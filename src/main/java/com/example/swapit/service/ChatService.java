package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomRequestDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;

public interface ChatService {
	void addChatRoom(ChatRoomRequestDto chatRoomRequestDto);

	List<ChatRoomResponseDto> getChatRoomList();

	ChatListDto getChatList(Long chatroomId, Long cursorId, LocalDateTime createdAt);

	ChatStompResponseDto saveChat(Long chatroomId, ChatStompRequestDto chatDto, String email);
}
