package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.RequesterGoodsDto;
import com.example.swapit.domain.dto.chat.ChatDto;
import com.example.swapit.domain.dto.chat.ChatListDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromGoodDto;
import com.example.swapit.domain.dto.chat.ChatRoomAddRequestFromTradeDto;
import com.example.swapit.domain.dto.chat.ChatRoomInfoDto;
import com.example.swapit.domain.dto.chat.ChatRoomResponseDto;
import com.example.swapit.domain.dto.chat.ChatStompRequestDto;
import com.example.swapit.domain.dto.chat.ChatStompResponseDto;
import com.example.swapit.domain.dto.good.TradeInfoDto;
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.ChatsRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.UsersRepository;
import com.example.swapit.repository.good.GoodsRepository;
import com.example.swapit.repository.trade.TradesRepository;
import com.example.swapit.service.notification.NotificationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final GoodsRepository goodsRepository;
	private final UsersRepository usersRepository;
	private final TradesRepository tradesRepository;
	private final CurrentUserService currentUserService;
	private final ChatRoomsRepository chatRoomsRepository;
	private final ChatsRepository chatRepository;
	private final GoodsImagesRepository goodsImagesRepository;
	private final NotificationEventPublisher notificationEventPublisher;
	private final SimpMessagingTemplate messagingTemplate;
	private final SimpUserRegistry simpUserRegistry;

	@Value("${cloud.aws.cloudfront.url}")
	private String cdnUrl;

	private static final int size = 30;

	@Override
	@Transactional
	public Long addChatRoomFromGood(ChatRoomAddRequestFromGoodDto chatRoomAddRequestFromGoodDto) {
		Goods goods = goodsRepository.findById(chatRoomAddRequestFromGoodDto.getGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Users inviter = currentUserService.getCurrentUser();

		// 같은 채팅방이 있는지 존재 검증 후, 생성
		ChatRooms chatroom = chatRoomsRepository.findByGoodsAndInviter(goods, inviter)
			.orElseGet(() -> chatRoomsRepository.save(new ChatRooms(goods, inviter, null)));

		return chatroom.getId();
	}

	@Override
	@Transactional
	public Long addChatRoomFromSwap(ChatRoomAddRequestFromTradeDto chatRoomAddRequestDto) {
		Trades trade = tradesRepository.findById(chatRoomAddRequestDto.getTradesId())
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 관계자인지 검증
		Users me = currentUserService.getCurrentUser();
		Long myId = me.getUsersId();
		
		Users targetOwner = trade.getTargetGoods().getUser();
		Users requestedOwner = trade.getRequestedGoods().getUser();
		if (!myId.equals(targetOwner.getUsersId()) && !myId.equals(requestedOwner.getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		// inviter는 항상 “goods 주인이 아닌 쪽”으로 설정
		Users inviter = targetOwner.equals(me) ? requestedOwner : me;
		Goods goods = trade.getTargetGoods();

		// 같은 채팅방이 있는지 존재 검증 후, 생성
		ChatRooms chatroom = chatRoomsRepository.findByGoodsAndInviter(goods, inviter)
			.orElseGet(() -> chatRoomsRepository.save(new ChatRooms(goods, inviter, trade)));

		return chatroom.getId();
	}

	@Override
	public List<ChatRoomResponseDto> getChatRoomList() {
		Long loggedInUserId = currentUserService.getCurrentUser().getUsersId();
		List<ChatRooms> chatRooms = chatRoomsRepository.findMyChatRooms(loggedInUserId);

		return chatRooms.stream()
			.filter(chatRoom -> chatRepository.countByChatRoomsId(chatRoom.getId()) > 0)
			.map(chatRoom -> {
				Users counterpart = getCounterpart(chatRoom);
				String userProfileImageUrl = counterpart.getProfileImageUrl().trim().startsWith("http")
					? counterpart.getProfileImageUrl() : cdnUrl + counterpart.getProfileImageUrl();

				Chats chats = chatRepository.findTopByChatRoomsIdOrderByCreatedAtDesc(chatRoom.getId());

				boolean isInviter = chatRoom.getInviter().getUsersId().equals(loggedInUserId);
				long lastReadId = isInviter ? chatRoom.getInviterLastReadId() : chatRoom.getNotInviterLastReadId();
				long unreadChatCount = chatRepository.findUnreadChatCount(chatRoom.getId(), lastReadId);

				return ChatRoomResponseDto.builder()
					.chatroomId(chatRoom.getId())
					.profileImageUrl(userProfileImageUrl)
					.nickname(counterpart.getNickname())
					.recentChat(chats.getContent())
					.recentChatTime(chats.getCreatedAt())
					.unReadChatCount(unreadChatCount)
					.build();
			}).toList();
	}

	@Override
	public ChatListDto getChatList(Long chatRoomId, Long cursorId, LocalDateTime createdAt) {

		List<Chats> chats = chatRepository.findAllByChatRoomsId(chatRoomId, cursorId, createdAt);

		boolean hasNext = chats.size() > size;

		List<Chats> paginateChats = hasNext ? chats.subList(0, size) : chats;

		Long lastCursorId = paginateChats.isEmpty() ? null : paginateChats.get(paginateChats.size() - 1).getId();

		List<ChatDto> chatList = chats.stream().map(chat -> {
			if (ChatType.REQUEST.equals(chat.getChatType()) || ChatType.ACCEPT.equals(chat.getChatType())) {
				Goods goods = goodsRepository.findById(chat.getGoodsId())
					.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

				RequesterGoodsDto requesterGoods = new RequesterGoodsDto(goods.getId(),
					goods.getTitle(),
					goods.getUser().getNickname()
				);
				return ChatDto.builder()
					.chatsId(chat.getId())
					.chatType(chat.getChatType())
					.content(chat.getContent())
					.requesterGoods(requesterGoods)
					.senderId(chat.getSender().getUsersId())
					.createdAt(chat.getCreatedAt())
					.build();
			} else {
				return ChatDto.builder()
					.chatsId(chat.getId())
					.chatType(chat.getChatType())
					.content(chat.getContent())
					.senderId(chat.getSender().getUsersId())
					.createdAt(chat.getCreatedAt())
					.build();
			}
		}).toList();

		return new ChatListDto(chatList, hasNext, lastCursorId, chatList.size());
	}

	@Override
	public ChatStompResponseDto saveChat(Long chatroomId, ChatStompRequestDto chatDto, Long userId) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));
		Users sender = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Chats chats = Chats.builder()
			.chatRooms(chatRooms)
			.sender(sender)
			.chatType(chatDto.getChatType())
			.content(chatDto.getContent())
			.goodsId(chatDto.getGoodsId())
			.build();

		RequesterGoodsDto requesterGoods = null;
		if (ChatType.REQUEST.equals(chats.getChatType()) || ChatType.ACCEPT.equals(chats.getChatType())) {
			Goods goods = goodsRepository.findById(chats.getGoodsId())
				.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

			requesterGoods = new RequesterGoodsDto(
				goods.getId(),
				goods.getTitle(),
				goods.getUser().getNickname()
			);
		}

		chatRepository.save(chats);

		// 알림 발행
		Long receiverId = chatRooms.getCounterpartId(sender.getUsersId());
		notificationEventPublisher.publishNotification(receiverId, NotificationType.CHAT, chatroomId);

		return new ChatStompResponseDto(chats, requesterGoods);
	}

	@Override
	public ChatRoomInfoDto getChatRoomInfo(Long chatroomId) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));
		Goods goods = chatRooms.getGoods();
		String firstImageUrl = goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods)
			.map(GoodsImages::getS3Key)
			.map(s3Key -> cdnUrl + s3Key)
			.orElse(null);
		Users counterpart = getCounterpart(chatRooms);

		TradeInfoDto tradeDto = chatRooms.getTrade() != null ? getTrade(chatRooms.getTrade()) : null;

		return new ChatRoomInfoDto(
			goods.getId(),
			goods.getTitle(),
			goods.getCategory().getName(),
			goods.getPrice(),
			firstImageUrl,
			counterpart.getUsersId(),
			counterpart.getNickname(),
			tradeDto
		);
	}

	@Override
	@Transactional
	public void updateReadReceipt(Long chatroomId, Long userId, Long lastReadChatId) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));

		boolean isInviter = chatRooms.getInviter().getUsersId().equals(userId);
		if (isInviter) {
			chatRooms.setInviterLastReadId(lastReadChatId);
		} else {
			chatRooms.setNotInviterLastReadId(lastReadChatId);
		}

		chatRoomsRepository.save(chatRooms);
	}

	public Users getCounterpart(ChatRooms chatRoom) {
		Long loggedInUserId = currentUserService.getCurrentUser().getUsersId();
		Users counterpart;
		Trades trade = chatRoom.getTrade();

		if (trade == null) {
			// 거래가 없는 채팅방
			// "채팅방의 물건 소유자" vs "inviter" 중, 내가 아닌 쪽이 상대방
			if (chatRoom.getGoods().getUser().getUsersId().equals(loggedInUserId)) {
				counterpart = chatRoom.getInviter();
			} else {
				counterpart = chatRoom.getGoods().getUser();
			}
		} else {
			// 거래가 있는 채팅방
			// targetGoods의 user가 나면, requestedGoods.user가 상대방
			// 반대면, targetGoods.user가 상대방
			if (trade.getTargetGoods().getUser().getUsersId().equals(loggedInUserId)) {
				counterpart = trade.getRequestedGoods().getUser();
			} else {
				counterpart = trade.getTargetGoods().getUser();
			}
		}

		return counterpart;
	}

	public TradeInfoDto getTrade(Trades trade) {
		if (trade == null) {
			return null;
		}
		Long loggedInUserId = currentUserService.getCurrentUser().getUsersId();
		boolean isRequester = trade.getRequestedGoods().getUser().getUsersId().equals(loggedInUserId);
		Long relatedGoodsId = isRequester ? trade.getTargetGoods().getId() : trade.getRequestedGoods().getId();
		String tradeStatus = trade.getStatus().name();
		return new TradeInfoDto(trade.getId(), isRequester, tradeStatus, relatedGoodsId);
	}
}
