package com.example.swapit.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.websocket.StompPrincipal;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;
import com.example.swapit.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatWebSocketController {
	private final SimpMessagingTemplate template;
	private final ChatService chatService;

	@MessageMapping("/chat/{chatroomId}")
	@SendTo("/topic/chat/{chatroomId}")
	public ChatStompResponseDto chat(@DestinationVariable Long chatroomId, ChatStompRequestDto message,
		Principal principal) {
		if (principal instanceof StompPrincipal stompPrincipal) {
			return chatService.saveChat(chatroomId, message, stompPrincipal.getEmail());
		} else {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}
}
