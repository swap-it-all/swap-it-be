package com.example.swapit.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.swapit.controller.ChatController;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.dto.ChatDto;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomGoodsDto;
import com.example.swapit.domain.dto.ChatRoomRequestDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.service.ChatServiceImpl;

public class ChatControllerDocsTest extends RestDocsTest {
	private final ChatServiceImpl chatService = mock(ChatServiceImpl.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected Object initController() {
		return new ChatController(chatService);
	}

	@Test
	@DisplayName("채팅방 생성 성공")
	void getChatRoomsSuccess() throws Exception {
		// Given
		ChatRoomRequestDto dto = new ChatRoomRequestDto(1L, 1L);
		doReturn(1L).when(chatService).addChatRoom(any(ChatRoomRequestDto.class));

		// When & Then
		mockMvc.perform(post("/api/user/chatroom")
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andDo(document("add-chatroom",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Chat")
					.description("채팅방을 생성하는 API")
					.requestFields(
						fieldWithPath("goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("requesterId").type(JsonFieldType.NUMBER).description("채팅 시작 사용자 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.NUMBER).description("응답 결과 데이터")
					)
					.requestSchema(Schema.schema(" ChatRoomRequestDto"))
					.responseSchema(Schema.schema("ApiResponse<Long>"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("채팅방 목록 조회 성공")
	void addChatRoomSuccess() throws Exception {
		// Given
		List<ChatRoomResponseDto> dtoList = new ArrayList<>();
		ChatRoomResponseDto dto = ChatRoomResponseDto.builder()
			.usersId(1L)
			.profileImageUrl("http://example.com/image.jpg")
			.nickname("testUser")
			.recentChat("안녕하세요!")
			.createdAt(LocalDateTime.now())
			.build();

		dtoList.add(dto);
		given(chatService.getChatRoomList()).willReturn(dtoList);

		// When & Then
		mockMvc.perform(get("/api/user/chatroom")
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("get-chatroom-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Chat")
					.description("채팅방 목록을 조회하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.chatRoomList").type(JsonFieldType.ARRAY).description("채팅방 목록"),
						fieldWithPath("results.chatRoomList[].usersId").type(JsonFieldType.NUMBER)
							.description("사용자 ID"),
						fieldWithPath("results.chatRoomList[].profileImageUrl").type(JsonFieldType.STRING)
							.description("프로필 이미지 URL"),
						fieldWithPath("results.chatRoomList[].nickname").type(JsonFieldType.STRING).description("닉네임"),
						fieldWithPath("results.chatRoomList[].recentChat").type(JsonFieldType.STRING)
							.description("최근 메세지"),
						fieldWithPath("results.chatRoomList[].createdAt").type(JsonFieldType.STRING).description("생성시각")
					)
					.responseSchema(Schema.schema("ChatRoomListResponseDto"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("채팅 내역 조회 성공")
	void getChatList() throws Exception {
		// Given
		Long chatroomId = 1L;
		ChatDto dto1 = ChatDto.builder()
			.chatsId(1L)
			.chatType(ChatType.TALK)
			.content("안녕하세요!")
			.senderId(1L)
			.createdAt(LocalDateTime.now())
			.build();

		ChatDto dto2 = ChatDto.builder()
			.chatsId(2L)
			.chatType(ChatType.TALK)
			.content("넵!")
			.senderId(2L)
			.createdAt(LocalDateTime.now())
			.build();

		ChatListDto chatListDto = new ChatListDto(List.of(dto1, dto2), true, 2L, 2);

		given(chatService.getChatList(anyLong(), any(), any())).willReturn(chatListDto);

		mockMvc.perform(get("/api/user/chatroom/{chatroomId}", chatroomId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("get-chat-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Chat")
					.description("채팅 내역을 조회하는 API")
					.pathParameters(
						parameterWithName("chatroomId").description("조회할 채팅방 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.chatList").type(JsonFieldType.ARRAY).description("채팅 목록"),
						fieldWithPath("results.chatList[].chatsId").type(JsonFieldType.NUMBER).description("채팅 ID"),
						fieldWithPath("results.chatList[].chatType").type(JsonFieldType.STRING).description("채팅 유형"),
						fieldWithPath("results.chatList[].content").type(JsonFieldType.STRING).description("채팅 내용"),
						fieldWithPath("results.chatList[].senderId").type(JsonFieldType.NUMBER).description("발신자 ID"),
						fieldWithPath("results.chatList[].createdAt").type(JsonFieldType.STRING)
							.description("채팅 생성 시각"),
						fieldWithPath("results.chatList[].requesterGoods").optional()
							.type(JsonFieldType.OBJECT)
							.description("스왑 요청 물건 정보 (대화 유형에 따라 존재)"),
						fieldWithPath("results.chatList[].requesterGoods.id").optional()
							.type(JsonFieldType.NUMBER)
							.description("스왑 요청 물건 ID"),
						fieldWithPath("results.chatList[].requesterGoods.title").optional()
							.type(JsonFieldType.STRING)
							.description("스왑 요청 물건 제목"),
						fieldWithPath("results.chatList[].requesterGoods.nickname").optional()
							.type(JsonFieldType.STRING)
							.description("스왑 요청 물건 작성자 닉네임"),
						fieldWithPath("results.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
						fieldWithPath("results.lastCursorId").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
						fieldWithPath("results.size").type(JsonFieldType.NUMBER).description("대화 목록 전체 개수")
					)
					.responseSchema(Schema.schema("ChatRoomListResponseDto"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("채팅방 물건 조회 성공")
	void getChatRoomGoods() throws Exception {
		// Given
		Long chatroomId = 1L;
		ChatRoomGoodsDto dto = new ChatRoomGoodsDto(
			1L, "아이폰 15", "ELECTRONICS", 1000000L, "http://example.com/image.jpg");

		given(chatService.getChatRoomGoods(any())).willReturn(dto);

		// When & Then
		mockMvc.perform(get("/api/user/chatroom/{chatroomId}/goods", chatroomId))
			.andExpect(status().isOk())
			.andDo(document("get-chat-room-goods",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Chat")
					.description("채팅 방의 물건을 조회하는 API")
					.pathParameters(
						parameterWithName("chatroomId").description("조회할 채팅방 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.price").type(JsonFieldType.NUMBER).description("물건 예상 가격"),
						fieldWithPath("results.imageUrl").type(JsonFieldType.STRING).description("물건 이미지 URL")
					)
					.responseSchema(Schema.schema("ChatRoomGoodsDto"))
					.build()
				)
			));
	}
}
