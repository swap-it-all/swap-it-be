package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

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
import com.example.swapit.domain.dto.ChatDto;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomAddRequestFromGoodDto;
import com.example.swapit.domain.dto.ChatRoomAddRequestFromTradeDto;
import com.example.swapit.domain.dto.ChatRoomGoodsDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;
import com.example.swapit.domain.dto.RequesterGoodsDto;
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.ChatsRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

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
	private final AwsS3Service awsS3Service;

	private static final int size = 30;

	@Override
	@Transactional
	public Long addChatRoomFromGood(ChatRoomAddRequestFromGoodDto chatRoomAddRequestFromGoodDto) {
		Goods goods = goodsRepository.findById(chatRoomAddRequestFromGoodDto.getGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Users requester = currentUserService.getCurrentUser();
		Users owner = usersRepository.findById(goods.getUser().getUsersId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 먼저 거래 조회 및 생성
		Trades trade = tradesRepository.findByRequesterAndTargetGoods(requester, goods)
			.orElseGet(() -> tradesRepository.save(new Trades(requester, goods)));

		// 같은 채팅방이 있는지 존재 검증 후, 생성
		ChatRooms chatroom = chatRoomsRepository.findByTrade(trade)
			.orElseGet(() -> chatRoomsRepository.save(new ChatRooms(trade)));

		return chatroom.getId();
	}

	@Override
	@Transactional
	public Long addChatRoomFromSwap(ChatRoomAddRequestFromTradeDto chatRoomAddRequestDto) {
		Trades trade = tradesRepository.findById(chatRoomAddRequestDto.getTradesId())
			.orElseThrow(() -> new CustomException(ErrorCode.TRADES_NOT_FOUND));

		// 거래 관계자인지 검증
		Users user = currentUserService.getCurrentUser();
		Long userId = user.getUsersId();
		if (!userId.equals(trade.getOwner().getUsersId()) && !userId.equals(trade.getRequester().getUsersId())) {
			throw new CustomException(ErrorCode.TRADE_UNAUTHORIZED);
		}

		// 같은 채팅방이 있는지 존재 검증 후, 생성
		ChatRooms chatroom = chatRoomsRepository.findByTrade(trade)
			.orElseGet(() -> chatRoomsRepository.save(new ChatRooms(trade)));

		return chatroom.getId();
	}

	@Override
	public List<ChatRoomResponseDto> getChatRoomList() {
		Long loggedInUserId = currentUserService.getCurrentUser().getUsersId();
		List<ChatRooms> chatRooms = chatRoomsRepository.findByUsersId(loggedInUserId);

		return chatRooms.stream()
			.filter(chatRoom -> chatRepository.countByChatRoomsId(chatRoom.getId()) > 0)
			.map(chatRoom -> {
				Long counterpartId =
					chatRoom.getOwner().getUsersId().equals(loggedInUserId) ? chatRoom.getRequester().getUsersId() :
						chatRoom.getOwner().getUsersId();

				Users counterpart = usersRepository.findById(counterpartId)
					.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

				String recentChat = chatRepository.findTopByChatRoomsIdOrderByCreatedAtDesc(chatRoom.getId())
					.getContent();
				LocalDateTime createdAt = chatRoom.getCreatedAt();

				return ChatRoomResponseDto.builder()
					.usersId(counterpartId)
					.profileImageUrl(counterpart.getProfileImageUrl())
					.nickname(counterpart.getNickname())
					.recentChat(recentChat)
					.createdAt(createdAt)
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
		Users users = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Chats chats = Chats.builder()
			.chatRooms(chatRooms)
			.sender(users)
			.chatType(chatDto.getChatType())
			.content(chatDto.getContent())
			.goodsId(chatDto.getGoodsId())
			.build();

		chatRepository.save(chats);

		// 알림 발행
		Long receiverId =
			(chatRooms.getRequester().getUsersId().equals(userId)) ? userId : chatRooms.getOwner().getUsersId();
		notificationEventPublisher.publishNotification(
			receiverId, NotificationType.CHAT, chatRooms.getId()
		);
		return new ChatStompResponseDto(chats);
	}

	@Override
	public ChatRoomGoodsDto getChatRoomGoods(Long chatroomId) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));
		Goods goods = chatRooms.getTargetGoods();
		String firstImageUrl = goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods)
			.map(GoodsImages::getS3Key)
			.map(awsS3Service::generatePreSignedImageUrl)
			.orElse(null);

		return new ChatRoomGoodsDto(
			goods.getId(),
			goods.getTitle(),
			goods.getCategory().getName(),
			goods.getPrice(),
			firstImageUrl
		);
	}
}