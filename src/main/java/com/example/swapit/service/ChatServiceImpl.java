package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.ChatDto;
import com.example.swapit.domain.dto.ChatListDto;
import com.example.swapit.domain.dto.ChatRoomRequestDto;
import com.example.swapit.domain.dto.ChatRoomResponseDto;
import com.example.swapit.domain.dto.ChatStompRequestDto;
import com.example.swapit.domain.dto.ChatStompResponseDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.domain.dto.RequesterGoodsDto;
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.ChatsRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final GoodsRepository goodsRepository;
	private final UsersRepository usersRepository;
	private final CurrentUserService currentUserService;
	private final ChatRoomsRepository chatRoomsRepository;
	private final ChatsRepository chatRepository;
	private final GoodsImagesRepository goodsImagesRepository;
	private final AwsS3Service awsS3Service;

	private static final int size = 30;

	@Override
	public Long addChatRoom(ChatRoomRequestDto chatRoomRequestDto) {
		Goods goods = goodsRepository.findById(chatRoomRequestDto.getGoodsId())
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Users requester = usersRepository.findById(chatRoomRequestDto.getRequesterId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Users owner = usersRepository.findById(goods.getUser().getUsersId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		ChatRooms chatRooms = ChatRooms.builder().goods(goods).owner(owner).requester(requester).build();

		return chatRoomsRepository.save(chatRooms).getId();
	}

	@Override
	public List<ChatRoomResponseDto> getChatRoomList() {
		Long loggedInUserId = currentUserService.getCurrentUser().getUsersId();
		List<ChatRooms> chatRooms = chatRoomsRepository.findByUsersId(loggedInUserId);

		return chatRooms.stream().map(chatRoom -> {
			Long counterpartId =
				chatRoom.getOwner().getUsersId().equals(loggedInUserId) ? chatRoom.getRequester().getUsersId() :
					chatRoom.getOwner().getUsersId();

			Users counterpart = usersRepository.findById(counterpartId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

			String recentChat = chatRepository.findTopByChatRoomsIdOrderByCreatedAtDesc(chatRoom.getId()).getContent();
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
	public ChatStompResponseDto saveChat(Long chatroomId, ChatStompRequestDto chatDto, String email) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));
		Users users = usersRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		Chats chats = Chats.builder()
			.chatRooms(chatRooms)
			.sender(users)
			.chatType(chatDto.getChatType())
			.content(chatDto.getContent())
			.goodsId(chatDto.getGoodsId())
			.build();

		chatRepository.save(chats);
		return new ChatStompResponseDto(chats);
	}

	@Override
	public GoodsDto getChatRoomGoods(Long chatroomId) {
		ChatRooms chatRooms = chatRoomsRepository.findById(chatroomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOMS_NOT_FOUND));
		Goods goods = chatRooms.getGoods();
		String firstImageUrl = goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods)
			.map(GoodsImages::getS3Key)
			.map(awsS3Service::generatePreSignedImageUrl)
			.orElse(null);

		return new GoodsDto(
			goods.getId(),
			goods.getTitle(),
			goods.getPrice(),
			goods.getCategory().getName(),
			firstImageUrl,
			goods.getPlaceName(),
			goods.getViewCount(),
			goods.getCreatedAt()
		);
	}
}
