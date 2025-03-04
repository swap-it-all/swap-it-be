package com.example.swapit.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.swapit.domain.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.domain.dto.trade.RequestGoodsImageDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;
import com.example.swapit.domain.dto.trade.TradesRequestDto;
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
	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;
	private final NotificationEventPublisher notificationEventPublisher;
	public static final int MAX_REQUEST_COUNT = 10;

	@Override
	@Transactional
	public void requestTrade(TradesRequestDto tradesRequestDto) {
		Goods requestedGoods = goodsRepository.findById(tradesRequestDto.getRequestedGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Goods targetGoods = goodsRepository.findById(tradesRequestDto.getTargetGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		long count = tradesRepository.countByTargetGoodsIdAndIsDeletedFalse(tradesRequestDto.getTargetGoodsId());

		if (count >= MAX_REQUEST_COUNT) {
			throw new CustomException(ErrorCode.MAXIMUM_TRADE_REQUEST);
		}

		try {
			tradesRepository.save(new Trades(requestedGoods, targetGoods));

			// 알림 발생
			notificationEventPublisher.publishNotification(
				targetGoods.getUser().getUsersId(), NotificationType.REQUESTED, targetGoods.getId());
		} catch (DataIntegrityViolationException ex) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}
	}

	@Override
	@Transactional
	public void cancelTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));
		tradesRepository.delete(trades);
	}

	@Override
	@Transactional
	public void acceptTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 owner 인지 검증
		if (!currentUserService.getCurrentUser().getUsersId().equals(trades.getTargetGoods().getUser().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		trades.setStatus(TradeStatus.INPROGRESS);

		// 같은 물건의 다른 거래 요청을 모두 REJECTED로 변경
		tradesRepository.rejectOtherTrades(trades.getTargetGoods(), tradesId);

		// 각 물건 상태를 reserved로 변경
		trades.getTargetGoods().setGoodsTradeStatus(GoodsTradeStatus.RESERVED);
		goodsRepository.save(trades.getTargetGoods());
		trades.getRequestedGoods().setGoodsTradeStatus(GoodsTradeStatus.RESERVED);
		goodsRepository.save(trades.getRequestedGoods());

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getRequesterId(), NotificationType.ACCEPTED, trades.getTargetGoods().getId()
		);
	}

	@Override
	@Transactional
	public void rejectTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 owner 인지 검증
		if (!currentUserService.getCurrentUser().getUsersId().equals(trades.getOwnerId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		trades.setStatus(TradeStatus.REJECTED);

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getRequesterId(), NotificationType.REJECTED, trades.getTargetGoods().getId()
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
		if (!userId.equals(trade.getOwnerId()) && !userId.equals(trade.getRequesterId())) {
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
		Users recipient = (userId.equals(trade.getOwnerId()))
				? trade.getRequestedGoods().getUser() : trade.getTargetGoods().getUser();
		notificationEventPublisher.publishNotification(
			recipient.getUsersId(), NotificationType.COMPLETED
		);
	}

	@Override
	public List<MyGoodsDto> getMyGoods() {
		List<Goods> goodsList = goodsRepository.findByUserOrderByCreatedAtDesc(currentUserService.getCurrentUser());
		List<Long> goodsIds = goodsList.stream().map(Goods::getId).toList();

		List<TradeCountProjection> tradeCounts = tradesRepository.findTradeCountByGoodsIds(goodsIds);
		Map<Long, Long> tradeCountMap = tradeCounts.stream()
			.collect(Collectors.toMap(TradeCountProjection::getGoodsId, TradeCountProjection::getTradeCount));

		return goodsList.stream()
			.map(goods -> new MyGoodsDto(
				goods.getId(),
				goods.getTitle(),
				goods.getPrice(),
				goods.getCategory().getName(),
				goods.getPlaceName(),
				getFirstImageUrl(goods),
				goods.getViewCount(),
				tradeCountMap.getOrDefault(goods.getId(), 0L),
				goods.getCreatedAt()
			))
			.toList();
	}

	@Override
	public List<ReceivedRequestDto> getGoodsRequests(Long goodsId) {
		List<Goods> goodsList = tradesRepository.findGoodsRequests(goodsId);

		return goodsList.stream()
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
	}

	@Override
	public List<MyRequestDto> getMyRequests() {
		List<RequestGoodsImageDto> dtoList = tradesRepository.findMyRequests(
			currentUserService.getCurrentUser().getUsersId());

		return dtoList.stream()
			.map(dto -> {
				String requestedGoodsPhotoUrl = getFirstImageUrl(dto.getRequestedGoods());
				String myGoodsPhotoUrl = getFirstImageUrl(dto.getMyGoods());

				return new MyRequestDto(
					dto.getRequestedGoods().getId(),
					dto.getRequestedGoods().getTitle(),
					dto.getRequestedGoods().getPrice(),
					dto.getRequestedGoods().getCategory().getName(),
					dto.getRequestedGoods().getPlaceName(),
					myGoodsPhotoUrl,
					requestedGoodsPhotoUrl
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
}