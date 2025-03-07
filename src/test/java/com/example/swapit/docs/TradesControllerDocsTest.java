package com.example.swapit.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.Mockito.*;
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

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.swapit.controller.TradesController;
import com.example.swapit.domain.dto.Result;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.domain.dto.trade.TradeMyGoodsRequestDto;
import com.example.swapit.domain.dto.trade.TradesRequestDto;
import com.example.swapit.service.TradesServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TradesControllerDocsTest extends RestDocsTest {

	private final TradesServiceImpl tradesService = mock(TradesServiceImpl.class);

	@Override
	protected Object initController() {
		return new TradesController(tradesService);
	}

	@Test
	@DisplayName("스왑 요청 성공")
	void requestSwapSuccess() throws Exception {
		// Given
		String token = "Bearer valid_token";
		TradesRequestDto dto = new TradesRequestDto(1L, 2L);

		when(tradesService.requestTrade(any(TradesRequestDto.class))).thenReturn(Result.success(1L));

		ObjectMapper objectMapper = new ObjectMapper();

		// When & Then
		mockMvc.perform(post("/api/user/swap/request")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andDo(document("swap-request",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑을 요청하는 API")
					.requestFields(
						fieldWithPath("requestedGoodsId").type(JsonFieldType.NUMBER).description("스왑 요청한 물건 ID"),
						fieldWithPath("targetGoodsId").type(JsonFieldType.NUMBER).description("스왑 요청 대상 물건 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.NUMBER).description("응답 결과 데이터")
					)
					.requestSchema(Schema.schema("TradesRequestDto"))
					.responseSchema(Schema.schema("ApiResponse<Long>"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("스왑 요청 취소 성공")
	void cancelSwapSuccess() throws Exception {
		// Given
		Long tradesId = 1L;
		doNothing().when(tradesService).cancelTrade(tradesId);

		// When & Then
		mockMvc.perform(delete("/api/user/swap/cancel/{tradesId}", tradesId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("swap-cancel",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑 요청을 취소하는 API")
					.pathParameters(
						parameterWithName("tradesId").description("취소할 스왑 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터").optional()
					)
					.responseSchema(Schema.schema("ApiResponse"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("거래 수락 성공")
	void acceptTradeSuccess() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).acceptTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/accept/{tradesId}", tradeId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("accept-swap",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑 요청을 수락하는 API")
					.pathParameters(
						parameterWithName("tradesId").description("수락할 스왑 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터").optional()
					)
					.responseSchema(Schema.schema("ApiResponse"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("거래 거절 성공")
	void rejectTradeSuccess() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).acceptTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/reject/{tradesId}", tradeId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("reject-swap",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑 요청을 거절하는 API")
					.pathParameters(
						parameterWithName("tradesId").description("거절할 스왑 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터").optional()
					)
					.responseSchema(Schema.schema("ApiResponse"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("거래 완료 성공")
	void completeTradeSuccess() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).acceptTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/complete/{tradesId}", tradeId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("complete-swap",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑을 완료하는 API")
					.pathParameters(
						parameterWithName("tradesId").description("완료할 스왑 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터").optional()
					)
					.responseSchema(Schema.schema("ApiResponse"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("스왑 목록의 내 물건 목록 조회 성공")
	void getSwapMyGoods() throws Exception {
		// given
		List<MyGoodsDto> goodsList = new ArrayList<>();
		MyGoodsDto dummyGoods = new MyGoodsDto(
			1L,
			"스타벅스 텀블러",
			35000L,
			"MISC",
			"경기도 안산시",
			"http://example.com/image.jpg",
			100L,
			5L,
			2L,
			LocalDateTime.now()
		);
		goodsList.add(dummyGoods);
		when(tradesService.getMyGoods()).thenReturn(goodsList);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-goods")
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andDo(document("get-swap-my-goods",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑 목록에서 내 물건 목록을 조회하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.goodsList").type(JsonFieldType.ARRAY).description("내 물건 목록"),
						fieldWithPath("results.goodsList[].goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.goodsList[].title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.goodsList[].price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("results.goodsList[].category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.goodsList[].placeName").type(JsonFieldType.STRING).description("물건 위치"),
						fieldWithPath("results.goodsList[].photoUrl").type(JsonFieldType.STRING)
							.description("물건 이미지 URL"),
						fieldWithPath("results.goodsList[].viewCount").type(JsonFieldType.NUMBER).description("조회수"),
						fieldWithPath("results.goodsList[].requestCount").type(JsonFieldType.NUMBER)
							.description("스왑 요청 수"),
						fieldWithPath("results.goodsList[].inProgressCount").type(JsonFieldType.NUMBER)
							.description("INPROGRESS 상태인 스왑 수"),
						fieldWithPath("results.goodsList[].createdAt").type(JsonFieldType.STRING).description("등록 시간")
					)
					.responseSchema(Schema.schema("ApiResponse<TradesGoodsListResponseDto<MyGoodsDto>>"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("내 물건에 스왑 요청 받은 물건 목록 조회 성공")
	void getMyGoodsRequest() throws Exception {
		// given
		Long goodsId = 1L;
		List<ReceivedRequestDto> requestList = new ArrayList<>();
		ReceivedRequestDto dummyGoods = new ReceivedRequestDto(
			1L,
			"스타벅스 텀블러",
			35000L,
			"MISC",
			"경기도 안산시",
			"http://example.com/image.jpg",
			LocalDateTime.now()
		);
		requestList.add(dummyGoods);
		TradeMyGoodsRequestDto dto = new TradeMyGoodsRequestDto("스타벅스 머그컵", requestList);
		when(tradesService.getGoodsRequests(goodsId)).thenReturn(dto);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-goods/{goodsId}/requests", goodsId)
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andDo(document("get-swap-my-goods-request",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("내 물건에 스왑 요청 받은 물건 목록을 조회하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.myGoodsTitle").type(JsonFieldType.STRING).description("내 물건 제목"),
						fieldWithPath("results.goodsList").type(JsonFieldType.ARRAY).description("물건 목록"),
						fieldWithPath("results.goodsList[].goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.goodsList[].title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.goodsList[].price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("results.goodsList[].category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.goodsList[].placeName").type(JsonFieldType.STRING).description("물건 위치"),
						fieldWithPath("results.goodsList[].photoUrl").type(JsonFieldType.STRING)
							.description("물건 이미지 URL"),
						fieldWithPath("results.goodsList[].createdAt").type(JsonFieldType.STRING).description("등록 시간")
					)
					.responseSchema(Schema.schema("TradeMyGoodsRequestDto"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("내가 보낸 스왑 요청 목록 조회 성공")
	void getMyRequest() throws Exception {
		// given
		List<MyRequestDto> requestList = new ArrayList<>();
		MyRequestDto dummyGoods = new MyRequestDto(
			1L,
			"스타벅스 텀블러",
			35000L,
			"MISC",
			"경기도 안산시",
			"http://example.com/my-goods.jpg",
			"http://example.com/requested-goods.jpg",
			100,
			LocalDateTime.now(),
			1L
		);
		requestList.add(dummyGoods);
		when(tradesService.getMyRequests()).thenReturn(requestList);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-requests")
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andDo(document("get-my-request",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Swap")
					.description("스왑 요청 보낸 물건 목록을 조회하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.goodsList").type(JsonFieldType.ARRAY).description("내 물건 목록"),
						fieldWithPath("results.goodsList[].goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.goodsList[].title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.goodsList[].price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("results.goodsList[].category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.goodsList[].placeName").type(JsonFieldType.STRING).description("물건 위치"),
						fieldWithPath("results.goodsList[].myGoodsPhotoUrl").type(JsonFieldType.STRING)
							.description("내 물건 이미지 URL"),
						fieldWithPath("results.goodsList[].targetGoodsPhotoUrl").type(JsonFieldType.STRING)
							.description("요청한 물건 이미지 URL"),
						fieldWithPath("results.goodsList[].targetGoodsViewCount").type(JsonFieldType.NUMBER)
							.description("요청한 물건 이미지 조회 수"),
						fieldWithPath("results.goodsList[].createdAt").type(JsonFieldType.STRING)
							.description("신청한 물건 등록 시간"),
						fieldWithPath("results.goodsList[].tradesId").type(JsonFieldType.NUMBER)
							.description("스왑 ID")
					)
					.responseSchema(Schema.schema("ApiResponse<TradesGoodsListResponseDto<MyRequestDto>>"))
					.build()
				)
			));
	}
}