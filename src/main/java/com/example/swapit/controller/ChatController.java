package com.example.swapit.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.websocket.StompPrincipal;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomRequestDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;
import com.example.swapit.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
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

	@PostMapping("/api/user/chatroom")
	public ApiResponse<Void> addChatRoom(@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
		chatService.addChatRoom(chatRoomRequestDto);
		return ApiResponse.success();
	}

	@GetMapping("/api/user/chatroom")
	public ApiResponse<List<ChatRoomResponseDto>> getChatRooms() {
		return ApiResponse.success(chatService.getChatRoomList());
	}

	@GetMapping("/api/user/chatroom/{chatroomId}")
	public ApiResponse<ChatListDto> getChats(@PathVariable Long chatroomId,
		@RequestParam(required = false) Long cursorId,                    // 마지막 조회 항목 ID
		@RequestParam(required = false) LocalDateTime createdAt        // 최신순일 경우, 커서 기준 값
	) {
		return ApiResponse.success(chatService.getChatList(chatroomId, cursorId, createdAt));
	}
}
