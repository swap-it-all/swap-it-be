package com.example.swapit.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomAddRequestFromGoodDto;
import com.example.swapit.domain.dto.ChatRoomAddRequestFromTradeDto;
import com.example.swapit.domain.dto.ChatRoomGoodsDto;
import com.example.swapit.domain.dto.ChatRoomListResponseDto;
import com.example.swapit.service.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	/**
	 * 거래가 없을 때 채팅방 생성
	 */
	@PostMapping("/api/user/chatroom")
	public ApiResponse<Long> addChatRoomWithGood(
		@Valid @RequestBody ChatRoomAddRequestFromGoodDto chatRoomAddRequestFromGoodDto) {
		return ApiResponse.success(chatService.addChatRoomFromGood(chatRoomAddRequestFromGoodDto));
	}

	/**
	 * 거래가 있을 때 채팅방 생성
	 */
	@PostMapping("/api/user/swap-chatroom")
	public ApiResponse<Long> addChatRoomWithTrade(
		@Valid @RequestBody ChatRoomAddRequestFromTradeDto chatRoomAddRequestFromTradeDto) {
		return ApiResponse.success(chatService.addChatRoomFromSwap(chatRoomAddRequestFromTradeDto));
	}

	@GetMapping("/api/user/chatroom")
	public ApiResponse<ChatRoomListResponseDto> getChatRooms() {
		return ApiResponse.success(new ChatRoomListResponseDto(chatService.getChatRoomList()));
	}

	@GetMapping("/api/user/chatroom/{chatroomId}")
	public ApiResponse<ChatListDto> getChats(@PathVariable Long chatroomId,
		@RequestParam(required = false) Long cursorId,                    // 마지막 조회 항목 ID
		@RequestParam(required = false) LocalDateTime createdAt        // 마지막 조회 항목의 생성일
	) {
		return ApiResponse.success(chatService.getChatList(chatroomId, cursorId, createdAt));
	}

	@GetMapping("/api/user/chatroom/{chatroomId}/goods")
	public ApiResponse<ChatRoomGoodsDto> getChatRoomGoods(@PathVariable Long chatroomId) {
		return ApiResponse.success(chatService.getChatRoomGoods(chatroomId));
	}
}