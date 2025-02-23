package com.example.swapit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.common.exception.GlobalExceptionHandler;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.service.TradesService;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
public class TradesControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private TradesController tradesController;

	@Mock
	private TradesService tradesService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(tradesController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("거래 요청 성공 테스트")
	void requestTradeSuccess() {
		// Given
		TradesRequestDto dto = new TradesRequestDto(1L, 2L);

		doNothing().when(tradesService).requestTrade(any(TradesRequestDto.class));

		// When
		ApiResponse<Void> response = tradesController.requestTrade(dto);

		// Then
		verify(tradesService, times(1)).requestTrade(dto);
		assertTrue(response.isSuccess());
		assertEquals("요청에 성공하였습니다.", response.getMessage());
	}

	@Test
	@DisplayName("거래 요청 실패 테스트")
	void requestTradeFailure() {
		// Given
		TradesRequestDto dto = new TradesRequestDto(null, 2L);

		// When
		CustomException exception = new CustomException(ErrorCode.GOOD_NOT_FOUND);
		doThrow(exception).when(tradesService).requestTrade(dto);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			tradesController.requestTrade(dto);
		});
		assertEquals(ErrorCode.GOOD_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("거래 삭제 성공 테스트")
	void deleteTradeSuccess() {
		// Given
		Long tradesId = 1L;

		doNothing().when(tradesService).cancelTrade(tradesId);

		// When
		ApiResponse<Void> response = tradesController.cancelTrade(tradesId);

		// Then
		verify(tradesService, times(1)).cancelTrade(tradesId);
		assertTrue(response.isSuccess());
		assertEquals("요청에 성공하였습니다.", response.getMessage());
	}

	@Test
	@DisplayName("거래 삭제 실패 테스트")
	void deleteTradeFailure() {
		// Given
		Long tradesId = null;

		// When
		CustomException exception = new CustomException(ErrorCode.TRADES_NOT_FOUND);
		doThrow(exception).when(tradesService).cancelTrade(tradesId);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			tradesController.cancelTrade(tradesId);
		});
		assertEquals(ErrorCode.TRADES_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("거래 수락 API 테스트 - 성공")
	void acceptTrade_ShouldReturnSuccess() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).acceptTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/accept/{tradesId}", tradeId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// 서비스 메서드가 1번 호출되었는지 검증
		verify(tradesService, times(1)).acceptTrade(tradeId);
	}

	@Test
	@DisplayName("거래 거절 API 테스트 - 성공")
	void rejectTrade_ShouldReturnSuccess() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).rejectTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/reject/{tradesId}", tradeId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(tradesService, times(1)).rejectTrade(tradeId);
	}

	@Test
	@DisplayName("거래 완료 성공 - 200 OK")
	void completeTrade_Success() throws Exception {
		// Given
		Long tradeId = 1L;
		doNothing().when(tradesService).completeTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/complete/{tradesId}", tradeId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(tradesService, times(1)).completeTrade(tradeId);
	}

	@Test
	@DisplayName("거래 완료 실패 - 거래를 찾을 수 없음 (404 NOT FOUND)")
	void completeTrade_Fail_TradeNotFound() throws Exception {
		// Given
		Long tradeId = 2L;
		CustomException exception = new CustomException(ErrorCode.TRADES_NOT_FOUND);
		doThrow(exception).when(tradesService).completeTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/complete/{tradesId}", tradeId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.TRADES_NOT_FOUND.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.TRADES_NOT_FOUND.getMessage()));

		verify(tradesService, times(1)).completeTrade(tradeId);
	}

	@Test
	@DisplayName("거래 완료 실패 - 거래 관계자가 아님 (403 FORBIDDEN)")
	void completeTrade_Fail_Unauthorized() throws Exception {
		// Given
		Long tradeId = 3L;
		doThrow(new CustomException(ErrorCode.TRADE_UNAUTHORIZED)).when(tradesService).completeTrade(tradeId);

		// When & Then
		mockMvc.perform(patch("/api/user/swap/complete/{tradesId}", tradeId))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.TRADE_UNAUTHORIZED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.TRADE_UNAUTHORIZED.getMessage()));

		verify(tradesService, times(1)).completeTrade(tradeId);
	}

	@Test
	@DisplayName("스왑 목록의 내 물건 목록 조회 성공")
	void testGetMyGoods() throws Exception {
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
			LocalDateTime.now()
		);
		goodsList.add(dummyGoods);
		when(tradesService.getMyGoods()).thenReturn(goodsList);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-goods"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andExpect(jsonPath("$.results.goodsList[0].goodsId").value(1L))
			.andExpect(jsonPath("$.results.goodsList[0].title").value("스타벅스 텀블러"));
	}

	@Test
	@DisplayName("내 물건에 스왑 요청 받은 물건 목록 조회 성공")
	void testGetGoodsRequests() throws Exception {
		// given
		Long goodsId = 1L;
		List<ReceivedRequestDto> requestList = new ArrayList<>();
		ReceivedRequestDto dummyRequest = new ReceivedRequestDto(
			1L,
			"스타벅스 머그컵",
			15000L,
			"MISC",
			"경기도 안산시",
			"http://example.com/request-image.jpg",
			LocalDateTime.now()
		);
		requestList.add(dummyRequest);
		when(tradesService.getGoodsRequests(goodsId)).thenReturn(requestList);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-goods/{goodsId}/requests", goodsId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andExpect(jsonPath("$.results.goodsList[0].goodsId").value(1L))
			.andExpect(jsonPath("$.results.goodsList[0].title").value("스타벅스 머그컵"));
	}

	@Test
	@DisplayName("내가 보낸 스왑 요청 목록 조회 성공")
	void testGetMyRequests() throws Exception {
		// given
		List<MyRequestDto> requestList = new ArrayList<>();
		MyRequestDto dummyMyRequest = new MyRequestDto(
			1L,
			"스타벅스 머그컵",
			15000L,
			"MISC",
			"경기도 안산시",
			"http://example.com/mygoods-image.jpg",
			"http://example.com/requestedgoods-image.jpg"
		);
		requestList.add(dummyMyRequest);
		when(tradesService.getMyRequests()).thenReturn(requestList);

		// when & then
		mockMvc.perform(get("/api/user/swap/my-requests"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.goodsList").isArray())
			.andExpect(jsonPath("$.results.goodsList[0].goodsId").value(1L))
			.andExpect(jsonPath("$.results.goodsList[0].title").value("스타벅스 머그컵"));
	}
}