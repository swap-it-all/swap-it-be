package com.example.swapit.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.ReceivedRequestDto;
import com.example.swapit.domain.dto.trade.RequestGoodsImageDto;
import com.example.swapit.domain.dto.trade.TradeCountProjection;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;

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

		// 기존 'NONE'이 아닌 거래가 있는지 확인 -> DUPLICATE 예외 발생
		boolean existingTradeExists = tradesRepository.existsByTargetGoodsAndStatusNot(targetGoods,
			TradeStatus.NONE);
		if (existingTradeExists) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}

		// 기존 거래에서 상태가 NONE인 거래가 있으면, 상태값 변경과 requestedGood만 추가
		Optional<Trades> existingTrade = tradesRepository.findByRequesterAndTargetGoods(requestedGoods.getUser(),
			targetGoods);
		Trades trade;
		if (existingTrade.isPresent()) {
			trade = existingTrade.get();
			trade.setStatus(TradeStatus.PENDING);
			trade.setRequestedGoods(requestedGoods);
		} else {
			// targetGoods의 PENDING 상태인 거래 개수가 한도 초과인지 확인.
			long count = tradesRepository.countByTargetGoodsIdAndIsDeletedFalse(tradesRequestDto.getTargetGoodsId());
			if (count >= MAX_REQUEST_COUNT) {
				throw new CustomException(ErrorCode.MAXIMUM_TRADE_REQUEST);
			}

			// 거래 횟수 초과되지 않으면, trade 생성.
			trade = new Trades(requestedGoods, targetGoods);
		}

		try {
			tradesRepository.save(trade);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}

		// 알림 발생
		notificationEventPublisher.publishNotification(
			targetGoods.getUser().getUsersId(), NotificationType.REQUESTED);
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
		if (!currentUserService.getCurrentUser().getUsersId().equals(trades.getOwner().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		trades.setStatus(TradeStatus.INPROGRESS);

		// 같은 물건의 다른 거래 요청을 모두 REJECTED로 변경
		tradesRepository.rejectOtherTrades(trades.getTargetGoods(), tradesId);

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getOwner().getUsersId(), NotificationType.ACCEPTED, trades.getTargetGoods().getId()
		);
	}

	@Override
	@Transactional
	public void rejectTrade(Long tradesId) {
		Trades trades = tradesRepository.findById(tradesId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 owner 인지 검증
		if (!currentUserService.getCurrentUser().getUsersId().equals(trades.getOwner().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		trades.setStatus(TradeStatus.REJECTED);

		// 알림 발생
		notificationEventPublisher.publishNotification(
			trades.getOwner().getUsersId(), NotificationType.REJECTED, trades.getTargetGoods().getId()
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
		if (!userId.equals(trade.getOwner().getUsersId()) && !userId.equals(trade.getRequester().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		trade.setStatus(TradeStatus.COMPLETED);
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