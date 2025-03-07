package com.example.swapit.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.dto.chat.ChatDto;
import com.example.swapit.domain.dto.chat.ChatListDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromGoodDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromTradeDto;
import com.example.swapit.domain.dto.chat.ChatRoomGoodsDto;
import com.example.swapit.domain.dto.chat.ChatRoomResponseDto;
import com.example.swapit.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private ChatController chatController;

	@Mock
	private ChatService chatService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
	}

	@Test
	@DisplayName("채팅방 생성 성공 - 물건 상세에서 요청")
	void addChatRoomWithGoodSuccess() throws Exception {
		// Given
		ChatRoomAddRequestFromGoodDto dto = new ChatRoomAddRequestFromGoodDto(1L);

		doReturn(1L).when(chatService)
			.addChatRoomFromGood(org.mockito.ArgumentMatchers.any(ChatRoomAddRequestFromGoodDto.class));

		// When & Then
		mockMvc.perform(post("/api/user/chatroom")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));

		verify(chatService, times(1)).addChatRoomFromGood(
			org.mockito.ArgumentMatchers.any(ChatRoomAddRequestFromGoodDto.class));
	}

	@Test
	@DisplayName("채팅방 생성 성공 - 거래 요청 내역에서 요청")
	void addChatRoomWithTrade_Success() throws Exception {
		// Given
		Long tradeId = 1L;
		Long expectedChatRoomId = 10L;
		ChatRoomAddRequestFromTradeDto requestDto = new ChatRoomAddRequestFromTradeDto(tradeId);

		when(chatService.addChatRoomFromSwap(any(ChatRoomAddRequestFromTradeDto.class)))
			.thenReturn(expectedChatRoomId);

		// When & Then
		mockMvc.perform(post("/api/user/swap-chatroom")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));

		// Verify
		verify(chatService, times(1)).addChatRoomFromSwap(any(ChatRoomAddRequestFromTradeDto.class));
	}

	@Test
	@DisplayName("채팅방 목록 조회 성공")
	void getChatRoomSuccess() throws Exception {
		// given
		ChatRoomResponseDto dto1 = ChatRoomResponseDto.builder()
			.usersId(1L)
			.profileImageUrl("http://example.com/image.jpg")
			.nickname("testUser")
			.recentChat("안녕하세요!")
			.build();

		ChatRoomResponseDto dto2 = ChatRoomResponseDto.builder()
			.usersId(2L)
			.profileImageUrl("http://example.com/image1.jpg")
			.nickname("testUser2")
			.recentChat("안녕하세요!!")
			.build();

		List<ChatRoomResponseDto> dtoList = List.of(dto1, dto2);

		given(chatService.getChatRoomList()).willReturn(dtoList);

		// when & then
		mockMvc.perform(get("/api/user/chatroom"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.chatRoomList", hasSize(2)))
			.andExpect(jsonPath("$.results.chatRoomList[0].usersId").value(1))
			.andExpect(jsonPath("$.results.chatRoomList[0].profileImageUrl").value("http://example.com/image.jpg"))
			.andExpect(jsonPath("$.results.chatRoomList[0].nickname").value("testUser"))
			.andExpect(jsonPath("$.results.chatRoomList[0].recentChat").value("안녕하세요!"))
			.andExpect(jsonPath("$.results.chatRoomList[1].usersId").value(2))
			.andExpect(jsonPath("$.results.chatRoomList[1].profileImageUrl").value("http://example.com/image1.jpg"))
			.andExpect(jsonPath("$.results.chatRoomList[1].nickname").value("testUser2"))
			.andExpect(jsonPath("$.results.chatRoomList[1].recentChat").value("안녕하세요!!"));
	}

	@Test
	@DisplayName("채팅 목록 조회 성공")
	void getChatsSuccess() throws Exception {
		// given
		ChatDto dto1 = ChatDto.builder()
			.chatsId(1L)
			.chatType(ChatType.TALK)
			.content("안녕하세요!")
			.build();

		ChatDto dto2 = ChatDto.builder()
			.chatsId(2L)
			.chatType(ChatType.TALK)
			.content("넵!")
			.build();

		ChatListDto chatListDto = new ChatListDto(List.of(dto1, dto2), true, 2L, 2);

		given(chatService.getChatList(any(), any(), any())).willReturn(chatListDto);

		// when & then
		mockMvc.perform(get("/api/user/chatroom/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.chatList").isArray())
			.andExpect(jsonPath("$.results.chatList[0].content").value("안녕하세요!"))
			.andExpect(jsonPath("$.results.chatList[1].content").value("넵!"))
			.andExpect(jsonPath("$.results.hasNext").value(true))
			.andExpect(jsonPath("$.results.lastCursorId").value(2));

		verify(chatService, times(1)).getChatList(any(), any(), any());
	}

	@Test
	@DisplayName("채팅방 물건 조회 성공")
	void getChatRoomGoods() throws Exception {
		// given
		ChatRoomGoodsDto dto = new ChatRoomGoodsDto(
			1L, "아이폰 15", "ELECTRONICS", 2500L, null);

		given(chatService.getChatRoomGoods(any())).willReturn(dto);

		// when & then
		mockMvc.perform(get("/api/user/chatroom/1/goods"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.title").value("아이폰 15"));

		verify(chatService, times(1)).getChatRoomGoods(any());
	}
}