package com.example.swapit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.service.TradesService;

@ExtendWith(MockitoExtension.class)
public class TradesControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private TradesController tradesController;

	@Mock
	private TradesService tradesService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(tradesController).build();
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
		assertNull(response.getResults());
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
		assertNull(response.getResults());
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
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/user/swap/accept/{tradesId}", tradeId)
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
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/user/swap/reject/{tradesId}", tradeId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(tradesService, times(1)).rejectTrade(tradeId);
	}
}