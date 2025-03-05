package com.example.swapit.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.GoodsTradeStatus;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.TradeGoodsDao;
import com.example.swapit.domain.dto.trade.InProgressCountDto;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradesServiceImpl implements TradesService {

	private final TradesRepository tradesRepository;
	private final GoodsRepository goodsRepository;
	private final GoodsImagesRepository goodsImagesRepository;
	private final ChatRoomsRepository chatRoomsRepository;
	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;
	private final ChatNotificationService chatNotificationService;
	private final NotificationEventPublisher notificationEventPublisher;
	public static final int MAX_REQUEST_COUNT = 10;

	@Override
	@Transactional
	public Long requestTrade(TradesRequestDto tradesRequestDto) {
		Goods requestedGoods = goodsRepository.findById(tradesRequestDto.getRequestedGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Goods targetGoods = goodsRepository.findById(tradesRequestDto.getTargetGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		// 최대 PENDING 요청 제한 체크
		long count = tradesRepository.countByTargetGoodsIdAndStatus(tradesRequestDto.getTargetGoodsId(),
			TradeStatus.PENDING);
		if (count >= MAX_REQUEST_COUNT) {
			throw new CustomException(ErrorCode.MAXIMUM_TRADE_REQUEST);
		}

		// 거래 생성
		Trades trades = new Trades(requestedGoods, targetGoods);
		Trades savedTrade;
		try {
			savedTrade = tradesRepository.save(trades);
			// 알림 발생
			notificationEventPublisher.publishNotification(
				targetGoods.getUser().getUsersId(), NotificationType.REQUESTED, targetGoods.getId());
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}

		// 채팅방이 존재하면 거래 연결 + 메시지 전송
		Optional<ChatRooms> chatRoomsOpt = chatRoomsRepository.findByGoodsAndInviter(targetGoods,
			requestedGoods.getUser());
		chatRoomsOpt.ifPresent(
			chatRooms -> updateChatroomAndSendChat(chatRooms, savedTrade, ChatType.REQUEST, requestedGoods)
		);

		return savedTrade.getId();
	}

	@Override
	@Transactional
	public void cancelTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 삭제
		tradesRepository.delete(trades);

		// 채팅방이 있으면 거래 해제 + 메시지 전송
		Optional<ChatRooms> chatRoomsOpt = chatRoomsRepository.findByTrade(trades);
		chatRoomsOpt.ifPresent(
			chatRooms -> updateChatroomAndSendChat(chatRooms, null, ChatType.CANCEL, trades.getRequestedGoods())
		);
	}

	@Override
	@Transactional
	public void acceptTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 물건 소유자(Owner)인지 검증
		Long currentUserId = currentUserService.getCurrentUser().getUsersId();
		if (!currentUserId.equals(trades.getTargetGoods().getUser().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		// 거래 상태 변경
		trades.setStatus(TradeStatus.INPROGRESS);

		// 같은 물건의 다른 거래 요청을 모두 REJECTED로 변경
		tradesRepository.rejectOtherTrades(trades.getTargetGoods(), tradesId);

		// 채팅방이 있으면 메시지 전송
		Optional<ChatRooms> chatRoomsOpt = chatRoomsRepository.findByTrade(trades);
		chatRoomsOpt.ifPresent(
			rooms -> updateChatroomAndSendChat(rooms, trades, ChatType.ACCEPT, trades.getRequestedGoods())
		);

		// 각 물건 상태를 reserved로 변경
		trades.getTargetGoods().setGoodsTradeStatus(GoodsTradeStatus.RESERVED);
		goodsRepository.save(trades.getTargetGoods());
		trades.getRequestedGoods().setGoodsTradeStatus(GoodsTradeStatus.RESERVED);
		goodsRepository.save(trades.getRequestedGoods());

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getRequestedGoods().getUser().getUsersId(),
			NotificationType.ACCEPTED,
			trades.getTargetGoods().getId()
		);
	}

	@Override
	@Transactional
	public void rejectTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 물건 소유자(Owner)인지 검증
		Long currentUserId = currentUserService.getCurrentUser().getUsersId();
		if (!currentUserId.equals(trades.getTargetGoods().getUser().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		// 거래 상태 변경
		trades.setStatus(TradeStatus.REJECTED);

		// 채팅방이 있으면 거래 해제 + 메시지 전송
		Optional<ChatRooms> chatRoomsOpt = chatRoomsRepository.findByTrade(trades);
		chatRoomsOpt.ifPresent(
			rooms -> updateChatroomAndSendChat(rooms, null, ChatType.REJECT, trades.getRequestedGoods())
		);

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getRequestedGoods().getUser().getUsersId(),
			NotificationType.REJECTED,
			trades.getTargetGoods().getId()
		);
	}

	@Override
	@Transactional
	public void completeTrade(Long tradesId) {
		Trades trade = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		Users user = currentUserService.getCurrentUser();
		Long userId = user.getUsersId();

		// 거래 관계자인지 확인
		if (!userId.equals(trade.getTargetGoods().getUser().getUsersId())
			&& !userId.equals(trade.getRequestedGoods().getUser().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		// 거래 완료 처리
		trade.setStatus(TradeStatus.COMPLETED);
		tradesRepository.save(trade);

		// 각 물건 거래 상태도 sold out 처리
		trade.getTargetGoods().setGoodsTradeStatus(GoodsTradeStatus.SOLDOUT);
		goodsRepository.save(trade.getTargetGoods());
		trade.getRequestedGoods().setGoodsTradeStatus(GoodsTradeStatus.SOLDOUT);
		goodsRepository.save(trade.getRequestedGoods());

		// 거래 상대방에게 알림 전송
		Users recipient = (userId.equals(trade.getTargetGoods().getUser().getUsersId()))
			? trade.getRequestedGoods().getUser() : trade.getTargetGoods().getUser();
		notificationEventPublisher.publishNotification(
			recipient.getUsersId(), NotificationType.COMPLETED
		);
	}

	@Override
	public List<MyGoodsDto> getMyGoods() {
		// 내 물건 목록 (최신순)
		List<Goods> goodsList = goodsRepository.findByUserOrderByCreatedAtDesc(currentUserService.getCurrentUser());
		List<Long> goodsIds = goodsList.stream().map(Goods::getId).toList();

		// 전체 거래 요청 수 조회
		List<TradeCountProjection> tradeCounts = tradesRepository.findTradeCountByGoodsIds(goodsIds);
		Map<Long, Long> tradeCountMap = tradeCounts.stream()
			.collect(Collectors.toMap(TradeCountProjection::getGoodsId, TradeCountProjection::getTradeCount));

		// INPROGRESS 거래 수 조회
		List<InProgressCountDto> inProgressCounts = tradesRepository.findInProgressCountByGoodsIds(goodsIds);
		Map<Long, Long> inProgressMap = inProgressCounts.stream()
			.collect(Collectors.toMap(InProgressCountDto::getGoodsId, InProgressCountDto::getInProgressCount));

		// MyGoodsDto 변환
		List<MyGoodsDto> result = goodsList.stream()
			.filter(goods -> tradeCountMap.getOrDefault(goods.getId(), 0L) != 0L)
			.map(goods -> new MyGoodsDto(
				goods.getId(),
				goods.getTitle(),
				goods.getPrice(),
				goods.getCategory().getName(),
				goods.getPlaceName(),
				getFirstImageUrl(goods),
				goods.getViewCount(),
				tradeCountMap.getOrDefault(goods.getId(), 0L), // 전체 거래 요청 수
				inProgressMap.getOrDefault(goods.getId(), 0L), // INPROGRESS 거래 수
				goods.getCreatedAt()
			))
			.collect(Collectors.toList());

		// 정렬 로직: INPROGRESS 거래 수가 1개 이상인 물건이 우선, 그 후 createdAt 내림차순
		result.sort((dto1, dto2) -> {
			boolean dto1HasInProgress = dto1.getInProgressCount() > 0;
			boolean dto2HasInProgress = dto2.getInProgressCount() > 0;

			// 1) INPROGRESS 거래가 있는 상품(dto1) vs 없는 상품(dto2)
			if (dto1HasInProgress && !dto2HasInProgress) {
				return -1; // dto1이 먼저
			} else if (!dto1HasInProgress && dto2HasInProgress) {
				return 1;  // dto2가 먼저
			}

			// 2) 둘 다 INPROGRESS가 있거나 없으면, createdAt 내림차순
			return dto2.getCreatedAt().compareTo(dto1.getCreatedAt());
		});

		return result;
	}

	@Override
	public TradeMyGoodsRequestDto getGoodsRequests(Long goodsId) {
		Goods myGoods = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		List<Goods> goodsList = tradesRepository.findGoodsRequests(goodsId);

		List<ReceivedRequestDto> list = goodsList.stream()
			.map(goods -> new ReceivedRequestDto(
				goods.getId(),
				goods.getTitle(),
				goods.getPrice(),
				goods.getCategory().getName(),
				goods.getPlaceName(),
				getFirstImageUrl(goods),
				goods.getCreatedAt()
			))
			.toList();

		return new TradeMyGoodsRequestDto(myGoods.getTitle(), list);
	}

	@Override
	public List<MyRequestDto> getMyRequests() {
		List<TradeGoodsDao> daoList = tradesRepository.findMyRequests(
			currentUserService.getCurrentUser().getUsersId());

		return daoList.stream()
			.map(dao -> {
				String targetGoodsPhotoUrl = getFirstImageUrl(dao.getTargetGoods());
				String myGoodsPhotoUrl = getFirstImageUrl(dao.getMyGoods());

				return new MyRequestDto(
					dao.getTargetGoods().getId(),
					dao.getTargetGoods().getTitle(),
					dao.getTargetGoods().getPrice(),
					dao.getTargetGoods().getCategory().getName(),
					dao.getTargetGoods().getPlaceName(),
					myGoodsPhotoUrl,
					targetGoodsPhotoUrl,
					dao.getTargetGoods().getViewCount(),
					dao.getTargetGoods().getCreatedAt()
				);
			})
			.toList();
	}

	private String getFirstImageUrl(Goods goods) {
		return goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods)
			.map(GoodsImages::getS3Key)
			.map(awsS3Service::generatePreSignedImageUrl)
			.orElse(null);
	}

	@Transactional
	protected void updateChatroomAndSendChat(ChatRooms chatRooms, Trades trade, ChatType chatType,
		Goods goodsForMessage) {
		chatRooms.updateTrade(trade);
		chatNotificationService.sendTradeRequestChat(chatRooms.getId(), chatType, goodsForMessage);
	}
}
