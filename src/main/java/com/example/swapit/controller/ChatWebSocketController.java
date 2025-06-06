package com.example.swapit.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.websocket.StompPrincipal;
import com.example.swapit.domain.dto.chat.ChatStompRequestDto;
import com.example.swapit.domain.dto.chat.ChatStompResponseDto;
import com.example.swapit.domain.dto.chat.ReadReceiptRequestDto;
import com.example.swapit.service.chat.ChatService;

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
			Long userId = Long.parseLong(stompPrincipal.getName());
			return chatService.saveChat(chatroomId, message, userId);
		} else {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}

	@MessageMapping("/chat/read/{chatroomId}")
	public void updateReadReceipt(@DestinationVariable Long chatroomId, @Payload ReadReceiptRequestDto receipt,
		Principal principal) {
		if (principal == null) {
			log.error("[ERROR] WebSocket 읽음 이벤트 처리 실패: Principal이 null입니다.");
			return;
		}

		log.debug("[WS 마지막메세지 id 저장] chatroomId={}, payload={}", chatroomId, receipt);

		Long userId = Long.parseLong(principal.getName());
		chatService.updateReadReceipt(chatroomId, userId, receipt.getLastReadChatId());
	}

	@MessageMapping("/chat/unsubscribe/{chatroomId}")
	public void unsubscribe(@DestinationVariable Long chatroomId, Principal principal) {
		Long userId = Long.parseLong(principal.getName());
		String dest = "/chat/" + chatroomId;

		// 채팅방 구독 취소
		SimpAttributesContextHolder.currentAttributes().removeAttribute("chatSub");
		log.debug("[UNSUBSCRIBE] 유저id={}, dest={}", userId, dest);
	}
}
