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
import org.springframework.dao.DataIntegrityViolationException;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.TradeStatus;
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
	@DisplayName("거래 요청 실패 - 요청된 거래가 10개 초과")
	void requestTradesMaximumFailure() {
		// Given
		Users requester = Users.builder()
			.email("email")
			.build();
		Goods requestedGoods = Goods.builder()
			.user(requester)
			.build();

		Users target = Users.builder()
			.email("email")
			.build();
		Goods targetGoods = Goods.builder()
			.user(target)
			.build();

		dto = new TradesRequestDto(1L, 2L);

		when(goodsRepository.findById(dto.getRequestedGoodsId())).thenReturn(Optional.of(requestedGoods));
		when(goodsRepository.findById(dto.getTargetGoodsId())).thenReturn(Optional.of(targetGoods));
		when(tradesRepository.countByTargetGoodsIdAndIsDeletedFalse(dto.getTargetGoodsId())).thenReturn(10L);

		// When
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.requestTrade(dto));
		assertEquals(ErrorCode.MAXIMUM_TRADE_REQUEST, exception.getErrorCode());

		// Then
		verify(goodsRepository, times(1)).findById(dto.getRequestedGoodsId());
		verify(goodsRepository, times(1)).findById(dto.getTargetGoodsId());
		verify(tradesRepository, times(1)).countByTargetGoodsIdAndIsDeletedFalse(dto.getTargetGoodsId());
		verify(tradesRepository, never()).save(any(Trades.class));
	}

	@Test
	@DisplayName("거래 요청 실패 - 중복 거래 요청")
	void requestTradesDuplicateFailure() {
		// Given
		Users requester = Users.builder()
			.email("email")
			.build();
		Goods requestedGoods = Goods.builder()
			.user(requester)
			.build();

		Users target = Users.builder()
			.email("email")
			.build();
		Goods targetGoods = Goods.builder()
			.user(target)
			.build();

		dto = new TradesRequestDto(1L, 2L);

		when(goodsRepository.findById(dto.getRequestedGoodsId())).thenReturn(Optional.of(requestedGoods));
		when(goodsRepository.findById(dto.getTargetGoodsId())).thenReturn(Optional.of(targetGoods));
		when(tradesRepository.countByTargetGoodsIdAndIsDeletedFalse(dto.getTargetGoodsId())).thenReturn(5L);
		doThrow(new DataIntegrityViolationException("Duplicate")).when(tradesRepository).save(any(Trades.class));

		// When
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.requestTrade(dto));
		assertEquals(ErrorCode.DUPLICATE_TRADE_REQUEST, exception.getErrorCode());

		// Then
		verify(goodsRepository, times(1)).findById(dto.getRequestedGoodsId());
		verify(goodsRepository, times(1)).findById(dto.getTargetGoodsId());
		verify(tradesRepository, times(1)).countByTargetGoodsIdAndIsDeletedFalse(dto.getTargetGoodsId());
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

	@Test
	@DisplayName("거래 수락 성공")
	void acceptTrade_Success() {
		// Given
		Long tradesId = 1L;
		Users requester = Users.builder()
			.email("email")
			.build();
		Goods requestedGood = Goods.builder()
			.user(requester)
			.build();
		Users target = Users.builder()
			.email("email")
			.build();
		Goods targetGood = Goods.builder()
			.user(target)
			.build();
		Trades trade = Trades.builder()
			.id(1L)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));
		doNothing().when(tradesRepository).rejectOtherTrades(targetGood, tradesId);

		// When
		tradesService.acceptTrade(tradesId);

		// Then
		assertEquals(TradeStatus.INPROGRESS, trade.getStatus());
		verify(tradesRepository, times(1)).rejectOtherTrades(targetGood, tradesId);
	}

	@Test
	@DisplayName("거래 수락 실패 - 거래를 찾을 수 없음")
	void acceptTrade_ThrowsException_WhenTradeNotFound() {
		// Given
		Long tradeId = 1L;
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.empty());

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.acceptTrade(tradeId));
		assertEquals(ErrorCode.TRADES_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("거래 거절 성공")
	void rejectTrade_Success() {
		// Given
		Long tradesId = 1L;
		Users requester = Users.builder()
			.email("email")
			.build();
		Goods requestedGood = Goods.builder()
			.user(requester)
			.build();
		Users target = Users.builder()
			.email("email")
			.build();
		Goods targetGood = Goods.builder()
			.user(target)
			.build();
		Trades trade = Trades.builder()
			.id(1L)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));

		// When
		tradesService.rejectTrade(tradesId);

		// Then
		assertEquals(TradeStatus.REJECTED, trade.getStatus());
	}

	@Test
	@DisplayName("거래 거절 실패 - 거래를 찾을 수 없음")
	void rejectTrade_ThrowsException_WhenTradeNotFound() {
		// Given
		Long tradeId = 1L;
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.empty());

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.rejectTrade(tradeId));
		assertEquals(ErrorCode.TRADES_NOT_FOUND, exception.getErrorCode());
	}
}