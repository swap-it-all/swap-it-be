package com.example.swapit.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomRequestDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	@PostMapping("/api/user/chatroom")
	public ApiResponse<Long> addChatRoom(@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
		return ApiResponse.success(chatService.addChatRoom(chatRoomRequestDto));
	}

	@GetMapping("/api/user/chatroom")
	public ApiResponse<List<ChatRoomResponseDto>> getChatRooms() {
		return ApiResponse.success(chatService.getChatRoomList());
	}

	@GetMapping("/api/user/chatroom/{chatroomId}")
	public ApiResponse<ChatListDto> getChats(@PathVariable Long chatroomId,
		@RequestParam(required = false) Long cursorId,                    // 마지막 조회 항목 ID
		@RequestParam(required = false) LocalDateTime createdAt        // 마지막 조회 항목의 생성일
	) {
		return ApiResponse.success(chatService.getChatList(chatroomId, cursorId, createdAt));
	}

	@GetMapping("/api/user/chatroom/{chatroomId}/goods")
	public ApiResponse<GoodsDto> getChatRoomGoods(@PathVariable Long chatroomId) {
		return ApiResponse.success(chatService.getChatRoomGoods(chatroomId));
	}
}
