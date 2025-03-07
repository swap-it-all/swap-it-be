package com.example.swapit.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.TradeGoodsDao;
import com.example.swapit.domain.dto.Result;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;
import com.example.swapit.domain.dto.trade.TradeMyGoodsRequestDto;
import com.example.swapit.domain.dto.trade.TradesRequestDto;
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;
import com.example.swapit.service.notification.NotificationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TradesServiceTest {

	@InjectMocks
	private TradesServiceImpl tradesService;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private GoodsImagesRepository goodsImagesRepository;

	@Mock
	private ChatRoomsRepository chatRoomsRepository;

	@Mock
	private CurrentUserServiceImpl currentUserService;

	@Mock
	private ChatNotificationService chatNotificationService;

	@Mock
	private NotificationEventPublisher notificationEventPublisher;

	private TradesRequestDto dto;

	private final String testCdnUrl = "http://example.com/";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(tradesService, "cdnUrl", testCdnUrl);
	}

	@Test
	@DisplayName("거래 요청 성공")
	void requestTradesSuccess() {
		// Given
		TradesRequestDto dto = new TradesRequestDto(1L, 2L);

		Users requester = Users.builder().usersId(10L).build();
		Users target = Users.builder().usersId(20L).build();

		Goods requestedGoods = Goods.builder().user(requester).build();
		Goods targetGoods = Goods.builder().user(target).build();

		when(goodsRepository.findById(dto.getRequestedGoodsId())).thenReturn(Optional.of(requestedGoods));
		when(goodsRepository.findById(dto.getTargetGoodsId())).thenReturn(Optional.of(targetGoods));

		when(tradesRepository.countByTargetGoodsIdAndStatus(dto.getTargetGoodsId(), TradeStatus.PENDING))
			.thenReturn(0L);

		when(tradesRepository.save(any(Trades.class))).thenAnswer(invocation -> {
			Trades trade = invocation.getArgument(0);
			ReflectionTestUtils.setField(trade, "id", 100L);
			return trade;
		});

		ChatRooms chatRoom = new ChatRooms(targetGoods, requester, null);
		ReflectionTestUtils.setField(chatRoom, "id", 200L);
		when(chatRoomsRepository.findByGoodsAndInviter(targetGoods, requester)).thenReturn(Optional.of(chatRoom));

		doNothing().when(notificationEventPublisher)
			.publishNotification(eq(target.getUsersId()), eq(NotificationType.REQUESTED), eq(targetGoods.getId()));
		doNothing().when(chatNotificationService)
			.sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.REQUEST), eq(requestedGoods));

		// When
		Result<Long> result = tradesService.requestTrade(dto);
		Long returnedTradeId = result.getData();

		// Then
		verify(goodsRepository).findById(dto.getRequestedGoodsId());
		verify(goodsRepository).findById(dto.getTargetGoodsId());
		verify(tradesRepository).save(any(Trades.class));
		verify(notificationEventPublisher).publishNotification(eq(target.getUsersId()), eq(NotificationType.REQUESTED),
			eq(targetGoods.getId()));
		verify(chatRoomsRepository).findByGoodsAndInviter(targetGoods, requester);
		verify(chatNotificationService).sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.REQUEST),
			eq(requestedGoods));

		assertThat(returnedTradeId).isEqualTo(100L);
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
		when(tradesRepository.countByTargetGoodsIdAndStatus(dto.getTargetGoodsId(), TradeStatus.PENDING)).thenReturn(
			10L);

		// When
		Result<Long> result = tradesService.requestTrade(dto);

		// Then
		assertFalse(result.isSuccess());
		assertEquals(ErrorCode.MAXIMUM_TRADE_REQUEST.getMessage(), result.getMessage());
		verify(goodsRepository, times(1)).findById(dto.getRequestedGoodsId());
		verify(goodsRepository, times(1)).findById(dto.getTargetGoodsId());
		verify(tradesRepository, times(1)).countByTargetGoodsIdAndStatus(dto.getTargetGoodsId(), TradeStatus.PENDING);
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
		when(tradesRepository.countByTargetGoodsIdAndStatus(dto.getTargetGoodsId(), TradeStatus.PENDING)).thenReturn(
			5L);
		doThrow(new DataIntegrityViolationException("Duplicate")).when(tradesRepository).save(any(Trades.class));

		// When
		Result<Long> result = tradesService.requestTrade(dto);

		// Then
		assertFalse(result.isSuccess());
		assertEquals(ErrorCode.DUPLICATE_TRADE_REQUEST.getMessage(), result.getMessage());
		verify(goodsRepository, times(1)).findById(dto.getRequestedGoodsId());
		verify(goodsRepository, times(1)).findById(dto.getTargetGoodsId());
		verify(tradesRepository, times(1)).countByTargetGoodsIdAndStatus(dto.getTargetGoodsId(), TradeStatus.PENDING);
		verify(tradesRepository, times(1)).save(any(Trades.class));
	}

	@Test
	@DisplayName("거래 요청 취소 성공")
	void cancelTradeSuccess() {
		// Given
		Long tradeId = 1L;
		Trades trade = Trades.builder().build();
		// 모킹: 해당 거래 조회
		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));
		// 채팅방이 존재하는 경우: Optional.of(chatRoom)
		ChatRooms chatRoom = new ChatRooms();
		ReflectionTestUtils.setField(chatRoom, "id", 200L);
		when(chatRoomsRepository.findByTrade(trade)).thenReturn(Optional.of(chatRoom));
		doNothing().when(chatNotificationService)
			.sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.CANCEL), any());

		// When & Then
		assertDoesNotThrow(() -> tradesService.cancelTrade(tradeId));
		verify(tradesRepository).delete(trade);
		verify(chatRoomsRepository).findByTrade(trade);
		verify(chatNotificationService).sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.CANCEL), any());
	}

	@Test
	@DisplayName("거래 수락 성공")
	void acceptTradeSuccess() {
		// Given
		Long tradesId = 1L;
		Users requester = Users.builder().usersId(2L).build();
		Users target = Users.builder().usersId(1L).build();
		Goods requestedGood = Goods.builder().user(requester).build();
		Goods targetGood = Goods.builder().user(target).build();
		Trades trade = Trades.builder()
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();
		ReflectionTestUtils.setField(trade, "id", tradesId);

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(target);

		doNothing().when(tradesRepository).rejectOtherTrades(targetGood, tradesId);
		// 모킹: 채팅방 존재함
		ChatRooms chatRoom = new ChatRooms(targetGood, requester, trade);
		ReflectionTestUtils.setField(chatRoom, "id", 300L);
		when(chatRoomsRepository.findByTrade(trade)).thenReturn(Optional.of(chatRoom));
		doNothing().when(chatNotificationService)
			.sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.ACCEPT), eq(requestedGood));

		// When
		assertDoesNotThrow(() -> tradesService.acceptTrade(tradesId));

		// Then
		// 거래 상태가 변경되었는지 확인
		assertThat(trade.getStatus()).isEqualTo(TradeStatus.INPROGRESS);
		verify(tradesRepository).rejectOtherTrades(targetGood, tradesId);
		verify(chatRoomsRepository).findByTrade(trade);
		verify(chatNotificationService).sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.ACCEPT),
			eq(requestedGood));
		// goods 상태도 변경되었는지 (RESERVED) – 실제 setter 호출 후 goodsRepository.save() 호출 여부를 검증
		verify(goodsRepository).save(targetGood);
		verify(goodsRepository).save(requestedGood);
		// 알림 발생 검증
		verify(notificationEventPublisher).publishNotification(eq(requestedGood.getUser().getUsersId()),
			eq(NotificationType.ACCEPTED), eq(targetGood.getId()));
	}

	@Test
	@DisplayName("거래 수락 성공 - 다른 거래들 거절 되었는지 확인")
	void acceptTrade_ShouldRejectAllOtherTrades() {
		// given
		Long acceptedTradeId = 1L;

		Users target = Users.builder().usersId(1L).build();

		Users requester1 = Users.builder().usersId(2L).build();
		Users requester2 = Users.builder().usersId(3L).build();
		Users requester3 = Users.builder().usersId(4L).build();

		Goods targetGood = Goods.builder().user(target).build();
		Goods requestedGood1 = Goods.builder().user(requester1).build();
		Goods requestedGood2 = Goods.builder().user(requester2).build();
		Goods requestedGood3 = Goods.builder().user(requester3).build();

		// 1. 거래 리스트 (거래를 요청한 사용자 3명)
		Trades acceptedTrade = Trades.builder()
			.id(acceptedTradeId)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood1)
			.build();

		Trades otherTrade1 = Trades.builder()
			.id(2L)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood2)
			.build();

		Trades otherTrade2 = Trades.builder()
			.id(3L)
			.targetGoods(targetGood)
			.requestedGoods(requestedGood3)
			.build();

		List<Trades> pendingTrades = Arrays.asList(acceptedTrade, otherTrade1, otherTrade2);

		when(tradesRepository.findById(acceptedTradeId)).thenReturn(Optional.of(acceptedTrade));
		when(currentUserService.getCurrentUser()).thenReturn(target);

		// Mock 설정: rejectOtherTrades() 호출 시 상태 변경 로직을 직접 수행
		doAnswer(invocation -> {
			Goods myTargetGood = invocation.getArgument(0);
			Long excludedTradeId = invocation.getArgument(1);

			// 다른 거래 요청들을 모두 REJECTED 상태로 변경
			pendingTrades.stream()
				.filter(trade -> trade.getTargetGoods().equals(myTargetGood) && !trade.getId().equals(excludedTradeId))
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
	void rejectTradeSuccess() {
		// Given
		Long tradesId = 1L;
		Users requester = Users.builder().usersId(2L).build();
		Users target = Users.builder().usersId(1L).build();
		Goods requestedGood = Goods.builder().user(requester).build();
		Goods targetGood = Goods.builder().user(target).build();
		Trades trade = Trades.builder()
			.targetGoods(targetGood)
			.requestedGoods(requestedGood)
			.status(TradeStatus.PENDING)
			.build();
		ReflectionTestUtils.setField(trade, "id", tradesId);

		when(tradesRepository.findById(tradesId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(target);

		// 모킹: 채팅방가 존재하는 경우
		ChatRooms chatRoom = new ChatRooms(targetGood, requester, trade);
		ReflectionTestUtils.setField(chatRoom, "id", 400L);
		when(chatRoomsRepository.findByTrade(trade)).thenReturn(Optional.of(chatRoom));
		doNothing().when(chatNotificationService)
			.sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.REJECT), eq(requestedGood));

		// When
		assertDoesNotThrow(() -> tradesService.rejectTrade(tradesId));

		// Then
		assertThat(trade.getStatus()).isEqualTo(TradeStatus.REJECTED);
		verify(chatRoomsRepository).findByTrade(trade);
		verify(chatNotificationService).sendTradeRequestChat(eq(chatRoom.getId()), eq(ChatType.REJECT),
			eq(requestedGood));
		verify(notificationEventPublisher).publishNotification(eq(requestedGood.getUser().getUsersId()),
			eq(NotificationType.REJECTED), eq(targetGood.getId()));
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

	@Test
	@DisplayName("스왑 목록의 내 물건 조회 성공")
	void testGetMyGoods() {
		// given
		Users user = Users.builder()
			.usersId(1L).build();
		when(currentUserService.getCurrentUser()).thenReturn(user);

		Categories categories = Categories.builder().name("MISC").build();

		Goods goods = Goods.builder()
			.user(user)
			.title("Item")
			.price(10000L)
			.category(categories)
			.build();

		ReflectionTestUtils.setField(goods, "id", 100L);

		List<Goods> goodsList = List.of(goods);
		when(goodsRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(goodsList);

		TradeCountProjection projection = new TradeCountProjection() {
			@Override
			public Long getGoodsId() {
				return 100L;
			}

			@Override
			public Long getTradeCount() {
				return 5L;
			}
		};
		when(tradesRepository.findTradeCountByGoodsIds(anyList())).thenReturn(List.of(projection));

		GoodsImages goodsImages = GoodsImages.builder().s3Key("s3Key123").build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods)).thenReturn(Optional.of(goodsImages));

		// when
		List<MyGoodsDto> result = tradesService.getMyGoods();

		// then
		assertEquals(1, result.size());
		MyGoodsDto dto = result.get(0);
		assertEquals("Item", dto.getTitle());
		assertEquals(10000L, dto.getPrice());
		assertEquals("MISC", dto.getCategory());
		assertEquals(testCdnUrl + goodsImages.getS3Key(), dto.getPhotoUrl());
		assertEquals(5L, dto.getRequestCount());
	}

	@Test
	@DisplayName("내 물건에 요청한 물건 조회 성공")
	void testGetGoodsRequests() {
		// given
		Long goodsId = 200L;
		Categories categories = Categories.builder()
			.name("MISC")
			.build();
		Goods myGoods = Goods.builder()
			.title("Item")
			.category(categories)
			.price(10000L)
			.build();

		ReflectionTestUtils.setField(myGoods, "id", goodsId);

		when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(myGoods));

		List<Goods> goodsList = List.of(myGoods);
		when(tradesRepository.findGoodsRequests(goodsId)).thenReturn(goodsList);

		GoodsImages goodsImages = GoodsImages.builder()
			.s3Key("s3Key")
			.build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(myGoods)).thenReturn(Optional.of(goodsImages));

		// when
		TradeMyGoodsRequestDto result = tradesService.getGoodsRequests(goodsId);

		// then
		assertNotNull(result);
		assertEquals("Item", result.getMyGoodsTitle());
		assertEquals(1, result.getGoodsList().size());

		ReceivedRequestDto dto = result.getGoodsList().get(0);
		assertEquals(goodsId.longValue(), dto.getGoodsId());
		assertEquals("Item", dto.getTitle());
		assertEquals(10000L, dto.getPrice());
		assertEquals("MISC", dto.getCategory());
		assertEquals(testCdnUrl + goodsImages.getS3Key(), dto.getPhotoUrl());
	}

	@Test
	@DisplayName("내가 요청한 물건 조회 성공")
	void testGetMyRequests() {
		// given
		Categories categories1 = Categories.builder().name("MISC").build();
		Goods requestedGoods = Goods.builder()
			.title("Requested Item")
			.category(categories1)
			.price(3000L).build();

		Categories categories2 = Categories.builder().name("MISC").build();
		Goods myGoods = Goods.builder()
			.title("My Item")
			.category(categories2)
			.price(4000L).build();

		TradeGoodsDao requestDto = new TradeGoodsDao(requestedGoods, myGoods, 1L);

		List<TradeGoodsDao> dtoList = List.of(requestDto);
		when(tradesRepository.findMyRequests(anyLong())).thenReturn(dtoList);
		Users dummyUser = Users.builder().usersId(1L).build();
		when(currentUserService.getCurrentUser()).thenReturn(dummyUser);

		GoodsImages requestedGoodsImage = GoodsImages.builder().s3Key("s3Key3").build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(requestedGoods))
			.thenReturn(Optional.of(requestedGoodsImage));

		GoodsImages myGoodsImage = GoodsImages.builder().s3Key("s3Key4").build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(myGoods))
			.thenReturn(Optional.of(myGoodsImage));
		// when
		List<MyRequestDto> result = tradesService.getMyRequests();

		// then
		assertEquals(1, result.size());
		MyRequestDto dto = result.get(0);
		assertEquals("Requested Item", dto.getTitle());
		assertEquals(3000L, dto.getPrice());
		assertEquals(testCdnUrl + myGoodsImage.getS3Key(), dto.getMyGoodsPhotoUrl());
		assertEquals(testCdnUrl + requestedGoodsImage.getS3Key(), dto.getTargetGoodsPhotoUrl());
	}
}