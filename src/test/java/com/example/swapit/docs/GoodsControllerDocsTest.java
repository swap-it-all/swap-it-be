package com.example.swapit.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.swapit.controller.GoodsController;
import com.example.swapit.domain.dto.Dto;
import com.example.swapit.domain.dto.good.GoodsDto;
import com.example.swapit.domain.dto.good.GoodsListDto;
import com.example.swapit.domain.dto.good.GoodsRequestDto;
import com.example.swapit.domain.dto.good.MyGoodDto;
import com.example.swapit.service.GoodsService;

public class GoodsControllerDocsTest extends RestDocsTest {
	private final GoodsService goodsService = mock(GoodsService.class);

	@Override
	protected Object initController() {
		return new GoodsController(goodsService);
	}

	@Test
	@DisplayName("모든 물건 조회")
	void getAllGoods() throws Exception {
		// given
		GoodsDto sampleGood = new GoodsDto(
			1L, "스타벅스 머그컵", 15000L, "MISC", "cup-image.jpg", "용산구",
			2300L, LocalDateTime.now());
		GoodsListDto response = new GoodsListDto(
			List.of(sampleGood), true, 5L, 10);

		when(goodsService.getGoods(any(), any(), any(), any(), any(), any())).thenReturn(response);

		// when & then
		mockMvc.perform(get("/api/all/goods")
				.param("cursorValue", "100")
				.param("cursorId", "5")
				.param("createdAt", "2024-03-06T12:00:00")
				.param("categoryIds", "1,2")
				.param("keyword", "스타벅스")
				.param("sortBy", "popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("get-all-goods",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Goods")
					.description("모든 물건을 조회하는 API. `sortBy` 값에 따라 필요한 필수 파라미터가 다름.")
					.queryParameters(
						parameterWithName("sortBy")
							.description("""
								    정렬 기준 (기본값: popular). <br>
								    - `recent` (최신순) 스크롤 내림➝ `cursorId` + `createdAt` 필수 <br>
								    - `popular`, `priceHigh`, `priceLow` 스크롤 내림 ➝ `cursorId` + `cursorValue` 필수
								""").optional(),
						parameterWithName("cursorId")
							.description("""
								    커서 기반 페이징을 위한 마지막 조회 항목 ID. <br>
								    - `recent` 사용 시 `createdAt`과 함께 필수 <br>
								    - `popular`, `priceHigh`, `priceLow` 사용 시 `cursorValue`와 함께 필수
								""").optional(),
						parameterWithName("createdAt")
							.description("""
								    최신순(`recent`) 정렬 시 커서 값. <br>
								    `cursorId`와 함께 필수.
								""").optional(),
						parameterWithName("cursorValue")
							.description("""
								    가격순(`priceHigh`, `priceLow`) 또는 인기순(`popular`) 정렬 시 커서 값. <br>
								    `cursorId`와 함께 필수.
								""").optional(),
						parameterWithName("categoryIds")
							.description("카테고리 필터 (예: `1,2,3`).").optional(),
						parameterWithName("keyword")
							.description("검색어 필터.").optional()
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("조회 결과"),
						fieldWithPath("results.goodsList").type(JsonFieldType.ARRAY).description("물건 목록"),
						fieldWithPath("results.goodsList[].goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.goodsList[].title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.goodsList[].price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("results.goodsList[].category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.goodsList[].imageUrl").type(JsonFieldType.STRING)
							.description("물건 이미지 URL"),
						fieldWithPath("results.goodsList[].placeName").type(JsonFieldType.STRING).description("거래 위치"),
						fieldWithPath("results.goodsList[].viewCount").type(JsonFieldType.NUMBER).description("조회 수"),
						fieldWithPath("results.goodsList[].createdAt").type(JsonFieldType.STRING).description("등록 시간"),
						fieldWithPath("results.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
						fieldWithPath("results.lastCursorId").type(JsonFieldType.NUMBER).description("마지막 조회된 물건 ID"),
						fieldWithPath("results.size").type(JsonFieldType.NUMBER).description("현재 페이지의 아이템 개수")
					)
					.responseSchema(Schema.schema("GoodsListDto"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("내 물건 목록 조회")
	void getMyAllGoods() throws Exception {
		// given
		List<MyGoodDto> docDtoList = List.of(
			new MyGoodDto(
				1L, "아이폰 16", 700_000L, "ELECTRONICS", "AVAILABLE",
				"iphoneImageUrl", "구로구", 1000L, LocalDateTime.now()
			));

		when(goodsService.getMyGoods("onsale")).thenReturn(new Dto<>(docDtoList));

		mockMvc.perform(get("/api/user/goods/my/{goodTradeStatus}", "onsale")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("get-my-all-goods",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Goods")
					.description("내 물건 목록을 조회하는 API")
					.pathParameters(
						parameterWithName("goodTradeStatus").description("조회할 물건 상태 (onsale, soldout)")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("내 물건 목록 데이터"),
						fieldWithPath("results.data").type(JsonFieldType.ARRAY).description("내 물건 목록"),
						fieldWithPath("results.data[].goodsId").type(JsonFieldType.NUMBER).description("물건 ID"),
						fieldWithPath("results.data[].title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("results.data[].price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("results.data[].category").type(JsonFieldType.STRING).description("물건 카테고리"),
						fieldWithPath("results.data[].goodTradeStatus").type(JsonFieldType.STRING).description("거래 상태"),
						fieldWithPath("results.data[].imageUrl").type(JsonFieldType.STRING).description("이미지 URL"),
						fieldWithPath("results.data[].placeName").type(JsonFieldType.STRING).description("거래 위치"),
						fieldWithPath("results.data[].viewCount").type(JsonFieldType.NUMBER).description("조회 수"),
						fieldWithPath("results.data[].createdAt").type(JsonFieldType.STRING).description("등록 시간")
					)
					.responseSchema(Schema.schema("Dto<List<MyGoodDto>>"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("물건 등록")
	void insertGood() throws Exception {
		GoodsRequestDto requestDto = new GoodsRequestDto("스타벅스 머그컵", 10000L, "NEW", 1L, "용산구", "팝니다 팔아요.");
		ObjectMapper objectMapper = new ObjectMapper();

		mockMvc.perform(post("/api/user/goods/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andDo(document("insert-good",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Goods")
					.description("새로운 물건을 등록하는 API")
					.requestFields(
						fieldWithPath("title").type(JsonFieldType.STRING).description("물건 제목"),
						fieldWithPath("price").type(JsonFieldType.NUMBER).description("물건 가격"),
						fieldWithPath("quality").type(JsonFieldType.STRING)
							.description("상태 (NEW, EXCELLENT, GOOD, FAIR, POOR)"),
						fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("물건 카테고리"),
						fieldWithPath("placeName").type(JsonFieldType.STRING).description("거래 희망 장소"),
						fieldWithPath("content").type(JsonFieldType.STRING).description("글 내용")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("응답 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.NUMBER).description("등록된 물건 ID")
					)
					.requestSchema(Schema.schema("GoodsRequestDto"))
					.responseSchema(Schema.schema("Long"))
					.build()
				)
			));
	}

}
