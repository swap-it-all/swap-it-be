package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
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

	@Mock
	private CurrentUserServiceImpl currentUserService;

	@Mock
	private NotificationEventPublisher notificationEventPublisher;

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
			.usersId(2L)
			.email("email")
			.build();
		Goods requestedGood = Goods.builder()
			.user(requester)
			.build();
		Users target = Users.builder()
			.usersId(1L)
			.email("email")
			.build();
		Goods targetGood = Goods.builder()
			.user(target)
			.build();
		Trades trade = Trades.builder()
			.id(1L)
			.owner(target)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(target);
		doNothing().when(tradesRepository).rejectOtherTrades(targetGood, tradesId);

		// When
		tradesService.acceptTrade(tradesId);

		// Then
		assertEquals(TradeStatus.INPROGRESS, trade.getStatus());
		verify(tradesRepository, times(1)).rejectOtherTrades(targetGood, tradesId);
	}

	@Test
	@DisplayName("거래 수락 성공 - 다른 거래들 거절 되었는지 확인")
	void acceptTrade_ShouldRejectAllOtherTrades() {
		// Given
		Long acceptedTradeId = 1L;
		Goods targetGood = Goods.builder()
			.build();
		Users target = Users.builder()
			.usersId(1L)
			.build();

		// 1. 거래 리스트 (거래를 요청한 사용자 3명)
		Trades acceptedTrade = Trades.builder()
			.id(acceptedTradeId)
			.owner(target)
			.targetGoods(targetGood)
			.build();

		Trades otherTrade1 = Trades.builder()
			.id(2L)
			.targetGoods(targetGood)
			.build();
		Trades otherTrade2 = Trades.builder()
			.id(3L)
			.targetGoods(targetGood)
			.build();

		List<Trades> pendingTrades = Arrays.asList(acceptedTrade, otherTrade1, otherTrade2);

		when(tradesRepository.findById(acceptedTradeId)).thenReturn(Optional.of(acceptedTrade));
		when(currentUserService.getCurrentUser()).thenReturn(target);

		// 3. Mock 설정: rejectOtherTrades() 호출 시 상태 변경 로직을 직접 수행
		doAnswer(invocation -> {
			Goods myTargetGood = invocation.getArgument(0);
			Long excludedTradeId = invocation.getArgument(1);

			// 다른 거래 요청들을 모두 REJECTED 상태로 변경
			pendingTrades.stream()
				.filter(
					trade -> trade.getTargetGoods().equals(myTargetGood) && !trade.getId().equals(excludedTradeId))
				.forEach(trade -> trade.setStatus(TradeStatus.REJECTED));

			return null;
		}).when(tradesRepository).rejectOtherTrades(targetGood, acceptedTradeId);

		// When
		tradesService.acceptTrade(acceptedTradeId);

		// Then
		assertEquals(TradeStatus.INPROGRESS, acceptedTrade.getStatus()); // 선택한 거래는 INPROGRESS
		assertEquals(TradeStatus.REJECTED, otherTrade1.getStatus()); // 다른 거래는 REJECTED
		assertEquals(TradeStatus.REJECTED, otherTrade2.getStatus()); // 다른 거래는 REJECTED

		// rejectOtherTrades() 메서드가 1번 호출되었는지 검증
		verify(tradesRepository, times(1)).rejectOtherTrades(targetGood, acceptedTradeId);
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
	@DisplayName("거래 수락 실패 - 거래 owner가 아닐 때 예외 발생")
	void acceptTrade_ShouldThrowException_WhenUserIsNotOwner() {
		// Given
		Long tradesId = 1L;
		Users owner = Users.builder().usersId(100L).nickname("owner").build();
		Users otherUser = Users.builder().usersId(200L).nickname("notOwner").build();
		Goods targetGoods = Goods.builder().user(owner).title("targetGood").build();
		Trades trades = new Trades(Goods.builder().user(owner).build(), targetGoods);

		// `findById(tradesId)`가 실행되면 `trades`를 반환하도록 설정
		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trades));

		// `currentUserService.getCurrentUser()`가 실행되면 `otherUser`를 반환하도록 설정
		when(currentUserService.getCurrentUser()).thenReturn(otherUser);

		// When & Then: `acceptTrade(tradesId)` 실행 시 `TRADE_UNAUTHORIZED` 예외가 발생해야 함
		CustomException exception = assertThrows(CustomException.class,
			() -> tradesService.acceptTrade(tradesId));

		// 예외 메시지 검증
		assertEquals(ErrorCode.TRADE_UNAUTHORIZED, exception.getErrorCode());
	}

	@Test
	@DisplayName("거래 거절 성공")
	void rejectTrade_Success() {
		// Given
		Long tradesId = 1L;
		Users requester = Users.builder()
			.usersId(2L)
			.email("email")
			.build();
		Goods requestedGood = Goods.builder()
			.user(requester)
			.build();
		Users target = Users.builder()
			.usersId(1L)
			.email("email")
			.build();
		Goods targetGood = Goods.builder()
			.user(target)
			.build();
		Trades trade = Trades.builder()
			.id(1L)
			.owner(target)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(target);

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

	@Test
	@DisplayName("거래 거절 실패 - 거래 owner가 아닐 때 예외 발생")
	void rejectTrade_ShouldThrowException_WhenUserIsNotOwner() {
		// Given
		Long tradesId = 1L;
		Users owner = Users.builder().usersId(100L).nickname("owner").build();
		Users otherUser = Users.builder().usersId(200L).nickname("notOwner").build();
		Goods targetGoods = Goods.builder().user(owner).title("targetGood").build();
		Trades trades = new Trades(Goods.builder().user(owner).build(), targetGoods);

		// `findById(tradesId)`가 실행되면 `trades`를 반환하도록 설정
		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trades));

		// `currentUserService.getCurrentUser()`가 실행되면 `otherUser`를 반환하도록 설정
		when(currentUserService.getCurrentUser()).thenReturn(otherUser);

		// When & Then: `acceptTrade(tradesId)` 실행 시 `TRADE_UNAUTHORIZED` 예외가 발생해야 함
		CustomException exception = assertThrows(CustomException.class,
			() -> tradesService.rejectTrade(tradesId));

		// 예외 메시지 검증
		assertEquals(ErrorCode.TRADE_UNAUTHORIZED, exception.getErrorCode());
	}

	@Test
	@DisplayName("거래 완료 성공 - 요청자가 거래를 완료할 수 있음")
	void completeTrade_Success_Requester() {
		// Given
		Long tradeId = 1L;
		Users owner = Users.builder().usersId(100L).nickname("owner").build();
		Users requester = Users.builder().usersId(200L).nickname("requester").build();
		Goods targetGoods = Goods.builder().user(owner).title("targetGood").build();
		Goods requestedGood = Goods.builder().user(requester).build();
		Trades trade = new Trades(requestedGood, targetGoods);
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(requester);

		// When
		tradesService.completeTrade(tradeId);

		// Then
		assertEquals(TradeStatus.COMPLETED, trade.getStatus());
		verify(tradesRepository, times(1)).findById(tradeId);
	}

	@Test
	@DisplayName("거래 완료 실패 - 거래를 찾을 수 없음")
	void completeTrade_Fail_TradeNotFound() {
		// Given
		Long tradeId = 3L;
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.empty());

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.completeTrade(tradeId));
		assertEquals(ErrorCode.TRADES_NOT_FOUND, exception.getErrorCode());
		verify(tradesRepository, times(1)).findById(tradeId);
	}

	@Test
	@DisplayName("거래 완료 실패 - 거래 관계자가 아님")
	void completeTrade_Fail_UnauthorizedUser() {
		// Given
		Long tradeId = 1L;
		Users owner = Users.builder().usersId(100L).nickname("owner").build();
		Users requester = Users.builder().usersId(200L).nickname("requester").build();
		Goods targetGoods = Goods.builder().user(owner).title("targetGood").build();
		Goods requestedGood = Goods.builder().user(requester).build();
		Trades trade = new Trades(requestedGood, targetGoods);
		Users otherUser = Users.builder().usersId(300L).nickname("otherUser").build();
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(otherUser);

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> tradesService.completeTrade(tradeId));
		assertEquals(ErrorCode.TRADE_UNAUTHORIZED, exception.getErrorCode());
		verify(tradesRepository, times(1)).findById(tradeId);
	}
}