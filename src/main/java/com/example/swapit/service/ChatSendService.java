package com.example.swapit.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.chat.ChatStompRequestDto;
import com.example.swapit.domain.dto.chat.ChatStompResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSendService {
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	private final CurrentUserService currentUserService;

	public void sendTradeRequestChat(Long chatRoomId, ChatType chatType, Goods goods) {
		Users user = currentUserService.getCurrentUser();
		ChatStompRequestDto requestDto = new ChatStompRequestDto(chatType, chatType.getMessage(), goods.getId());
		ChatStompResponseDto responseDto = chatService.saveChat(chatRoomId, requestDto, user.getUsersId());

		log.debug("[SWAP CHAT SEND] chatRoomId={}, userId={}, chatType={}, goodsId={}, content=\"{}\"",
			chatRoomId, user.getUsersId(), chatType.name(), goods.getId(), responseDto.getContent());

		messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, responseDto);
	}
}
