package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;

@ExtendWith(MockitoExtension.class)
public class TradesServiceTest {

	@InjectMocks
	private TradesServiceImpl tradesService;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private GoodsRepository goodsRepository;

	private TradesRequestDto dto;

	@Test
	@DisplayName("거래 요청 성공")
	void requestTradesSuccess() {
		// Given
		Users requester = Users.builder()
			.nickname("nickname")
			.email("email")
			.loginInfo("google")
			.build();
		Goods requestedGoods = Goods.builder()
			.user(requester)
			.build();

		Users target = Users.builder()
			.nickname("nickname")
			.email("email")
			.loginInfo("google")
			.build();
		Goods targetGoods = Goods.builder()
			.user(target)
			.build();

		dto = new TradesRequestDto(1L, 2L);

		when(goodsRepository.findById(dto.getRequestedGoodsId())).thenReturn(Optional.of(requestedGoods));
		when(goodsRepository.findById(dto.getTargetGoodsId())).thenReturn(Optional.of(targetGoods));
		when(tradesRepository.save(any(Trades.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		assertDoesNotThrow(() -> tradesService.requestTrade(dto));

		// Then
		verify(goodsRepository, times(1)).findById(dto.getRequestedGoodsId());
		verify(goodsRepository, times(1)).findById(dto.getTargetGoodsId());
		verify(tradesRepository, times(1)).save(any(Trades.class));
	}

	@Test
	@DisplayName("거래 요청 취소 성공")
	void cancelTradesSuccess() {
		// Given
		Long tradeId = 1L;
		Trades trade = Trades.builder().build();
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));

		// When
		assertDoesNotThrow(() -> tradesService.cancelTrade(tradeId));

		// Then
		verify(tradesRepository, times(1)).delete(trade);
	}
}