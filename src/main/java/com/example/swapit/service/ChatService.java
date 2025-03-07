package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.swapit.domain.dto.chat.ChatListDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromGoodDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromTradeDto;
import com.example.swapit.domain.dto.chat.ChatRoomGoodsDto;
import com.example.swapit.domain.dto.chat.ChatRoomResponseDto;
import com.example.swapit.domain.dto.chat.ChatStompRequestDto;
import com.example.swapit.domain.dto.chat.ChatStompResponseDto;

public interface ChatService {
	Long addChatRoomFromGood(ChatRoomAddRequestFromGoodDto chatRoomAddRequestFromGoodDto);

	Long addChatRoomFromSwap(ChatRoomAddRequestFromTradeDto chatRoomAddRequestFromTradeDto);

	List<ChatRoomResponseDto> getChatRoomList();

	ChatListDto getChatList(Long chatroomId, Long cursorId, LocalDateTime createdAt);

	ChatStompResponseDto saveChat(Long chatroomId, ChatStompRequestDto chatDto, Long userId);

	ChatRoomGoodsDto getChatRoomGoods(Long chatroomId);
}