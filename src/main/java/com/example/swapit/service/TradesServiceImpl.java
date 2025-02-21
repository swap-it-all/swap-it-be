package com.example.swapit.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradesServiceImpl implements TradesService {

	private final TradesRepository tradesRepository;
	private final GoodsRepository goodsRepository;
	private final CurrentUserService currentUserService;
	private final NotificationEventPublisher notificationEventPublisher;
	public static final int MAX_REQUEST_COUNT = 10;

	@Override
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
				targetGoods.getUser().getUsersId(), NotificationType.REQUESTED);
		} catch (DataIntegrityViolationException ex) {
			throw new CustomException(ErrorCode.DUPLICATE_TRADE_REQUEST);
		}
	}

	@Override
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
}