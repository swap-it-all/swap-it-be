package com.example.swapit.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatNotificationService {
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	private final CurrentUserService currentUserService;

	public void sendTradeRequestChat(Long chatRoomId, ChatType chatType, Goods goods) {
		ChatStompRequestDto requestDto = new ChatStompRequestDto(chatType, chatType.getMessage(), goods.getId());
		ChatStompResponseDto responseDto = chatService.saveChat(chatRoomId, requestDto,
			currentUserService.getCurrentUser().getUsersId());
		messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, responseDto);
	}
}
