package com.example.swapit.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
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

import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.domain.dto.GoodsImageDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;
import com.example.swapit.domain.dto.UserProfileDto;
import com.example.swapit.service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GoodsControllerTest {

	private MockMvc mockMvc;

	@Mock
	private GoodsService goodsService;

	@InjectMocks
	private GoodsController goodsController;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(goodsController).build();
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	@DisplayName("물건 목록 조회 테스트")
	void getAllGoods() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime tenMinutesAgo = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);

		GoodsDto dto1 = new GoodsDto(
			1L, "아이폰 15", 2500L, "NEW", null, null, 10L, now
		);
		GoodsDto dto2 = new GoodsDto(
			2L, "갤럭시 S25", 1300L, "FAIR", null, null, 100L, tenMinutesAgo
		);

		GoodsListDto mockGoodsList = new GoodsListDto(List.of(dto1, dto2), true, 2L, 2);
		given(goodsService.getGoods(any(), any(), any(), any(), any(), any())).willReturn(mockGoodsList);

		// when & then
		mockMvc.perform(get("/api/all/goods")
				.param("sortBy", "recent")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andExpect(jsonPath("$.results.goodsList[0].title").value("아이폰 15"))
			.andExpect(jsonPath("$.results.goodsList[1].title").value("갤럭시 S25"))
			.andExpect(jsonPath("$.results.hasNext").value(true))
			.andExpect(jsonPath("$.results.lastCursorId").value(2));
	}

	@Test
	@DisplayName("물건 상세 조회 테스트")
	void getDetailGood() throws Exception {
		// given
		GoodsDetailDto mockGoodsDetail = new GoodsDetailDto(
			1L,
			new UserProfileDto(1L, "testUser", 4.8),
			"전자기기",
			"아이폰 15",
			1200L,
			"새 상품",
			"좋은 상태입니다.",
			"판매 중",
			"서울 강남구",
			120,
			List.of(new GoodsImageDto(1L, "imageUrl")),
			LocalDateTime.now()
		);

		// given(goodsService.getGoodDetail(anyLong())).willReturn(mockGoodsDetail);
		given(goodsService.getGoodDetail(1L)).willReturn(mockGoodsDetail);

		mockMvc.perform(get("/api/all/goods/{goodsId}", 1L))
			.andDo(print())
			.andExpect(status().isOk());

		// when & then
		mockMvc.perform(get("/api/all/goods/{goodsId}", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.goodsId").value(1L))
			.andExpect(jsonPath("$.results.title").value("아이폰 15"))
			.andExpect(jsonPath("$.results.category").value("전자기기"))
			.andExpect(jsonPath("$.results.price").value(1200L))
			.andExpect(jsonPath("$.results.quality").value("새 상품"))
			.andExpect(jsonPath("$.results.content").value("좋은 상태입니다."))
			.andExpect(jsonPath("$.results.goodsTradeStatus").value("판매 중"))
			.andExpect(jsonPath("$.results.placeName").value("서울 강남구"))
			.andExpect(jsonPath("$.results.viewCount").value(120))
			.andExpect(jsonPath("$.results.images").isArray());
	}

	@Test
	@DisplayName("내 물건 목록 조회 API 테스트")
	void getMyAllGoods() throws Exception {
		// given
		List<GoodsDto> mockGoodsList = List.of(
			new GoodsDto(1L, "Laptop", 1000L, "Electronics", "/images/1", null, 100, LocalDateTime.now()),
			new GoodsDto(2L, "Phone", 500L, "Mobile", "/images/2", null, 100, LocalDateTime.now())
		);
		when(goodsService.getMyGoods()).thenReturn(mockGoodsList);

		// When & Then
		mockMvc.perform(get("/api/user/goods/my")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results", hasSize(2)))
			.andExpect(jsonPath("$.results[0].title").value("Laptop"))
			.andExpect(jsonPath("$.results[1].title").value("Phone"));
	}

	@Test
	@DisplayName("물건 등록 테스트")
	void insertGood() throws Exception {
		// given
		Map<String, Object> requestDto = Map.of(
			"title", "아이폰 15",
			"quality", "NEW",
			"price", 1200L,
			"categoryId", 1L,
			"placeName", "용산역 1번출구",
			"content", "좋은 상태입니다."
		);
		when(goodsService.insertGood(any(GoodsRequestDto.class))).thenReturn(1L);

		// when & then
		mockMvc.perform(post("/api/user/goods/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));
	}

	@Test
	@DisplayName("물건 수정 테스트")
	void updateGood() throws Exception {
		// given
		Map<String, Object> requestDto = Map.of(
			"title", "아이폰 15 Pro",
			"quality", "NEW",
			"price", 1500L,
			"categoryId", 1L,
			"placeName", "용산역 4번출구",
			"content", "최고 상태입니다."
		);
		doNothing().when(goodsService).updateGood(anyLong(), any(GoodsRequestDto.class));

		// when & then
		mockMvc.perform(put("/api/user/goods/{goodsId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));
	}

	@Test
	@DisplayName("물건 삭제 테스트")
	void deleteGood() throws Exception {
		// given
		doNothing().when(goodsService).deleteGood(anyLong());

		// when & then
		mockMvc.perform(delete("/api/user/goods/{goodsId}", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));

		// deleteGood() 메서드가 실행되었는지 검증
		verify(goodsService, times(1)).deleteGood(anyLong());
	}
}