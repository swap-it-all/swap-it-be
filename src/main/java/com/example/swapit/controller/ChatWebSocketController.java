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
import com.example.swapit.domain.dto.chat.ChatStompRequestDto;
import com.example.swapit.domain.dto.chat.ChatStompResponseDto;
import com.example.swapit.domain.dto.chat.ReadReceiptRequestDto;
import com.example.swapit.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatWebSocketController {
	private final SimpMessagingTemplate template;
	private final ChatService chatService;

	@MessageMapping("/chat/{chatroomId}")
	@SendTo("/topic/chat/{chatroomId}")
	public ChatStompResponseDto chat(@DestinationVariable Long chatroomId, ChatStompRequestDto message,
		Principal principal) {
		if (principal == null) {
			log.error("[ERROR] WebSocket 메시지 처리 실패: Principal이 null입니다.");
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		if (principal instanceof StompPrincipal stompPrincipal) {
			log.info("[MESSAGE] Principal 설정 완료: userId = {}", stompPrincipal.getName());
			Long userId = Long.parseLong(stompPrincipal.getName());
			return chatService.saveChat(chatroomId, message, userId);
		} else {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}

	@MessageMapping("/chat/read/{chatroomId}")
	public void updateReadReceipt(@DestinationVariable Long chatroomId, ReadReceiptRequestDto receipt,
		Principal principal) {
		if (principal == null) {
			log.error("[ERROR] WebSocket 읽음 이벤트 처리 실패: Principal이 null입니다.");
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		Long userId = Long.parseLong(principal.getName());
		chatService.updateReadReceipt(chatroomId, userId, receipt.getLastReadChatId());
	}
}