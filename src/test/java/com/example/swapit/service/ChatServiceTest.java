package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.domain.Categories;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.GoodsQuality;
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
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.ChatsRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.TradesRepository;
import com.example.swapit.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private ChatRoomsRepository chatRoomsRepository;

	@Mock
	private ChatsRepository chatRepository;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private GoodsImagesRepository goodsImagesRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private AwsS3Service awsS3Service;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private NotificationEventPublisher notificationEventPublisher;

	@InjectMocks
	private ChatServiceImpl chatService;

	@Test
	@DisplayName("물건 기반 채팅방 생성 테스트")
	void addChatRoomFromGood() {
		// given
		ChatRoomAddRequestFromGoodDto requestDto = new ChatRoomAddRequestFromGoodDto(1L);

		Users requester = Users.builder()
			.usersId(1L)
			.email("test@example.com")
			.build();

		Users owner = Users.builder()
			.usersId(2L)
			.email("test@example.com")
			.build();

		Goods requestedGood = Goods.builder()
			.title("물건")
			.price(2000L)
			.quality(GoodsQuality.NEW)
			.content("싸게 드려요!")
			.user(owner)
			.build();

		Goods targetGood = Goods.builder()
			.title("test 물건")
			.price(1000L)
			.quality(GoodsQuality.NEW)
			.content("싸게 드려요! 교환주세요!")
			.user(owner)
			.build();
		when(goodsRepository.findById(1L)).thenReturn(Optional.of(targetGood));
		when(usersRepository.findById(2L)).thenReturn(Optional.of(owner));
		when(currentUserService.getCurrentUser()).thenReturn(requester);

		Trades trade = new Trades(requestedGood, targetGood);
		doReturn(Optional.of(trade)).when(tradesRepository)
			.findByRequestedGoodsUserAndTargetGoods(eq(requester), eq(targetGood));

		ChatRooms chatRoom = new ChatRooms(targetGood, requester, trade);
		when(chatRoomsRepository.save(any(ChatRooms.class))).thenReturn(chatRoom);

		// when
		Long chatRoomId = chatService.addChatRoomFromGood(requestDto);

		// then
		verify(chatRoomsRepository, times(1)).save(any(ChatRooms.class));
		assertEquals(chatRoom.getGoods(), targetGood);
		assertEquals(chatRoom.getInviter(), requester);
		assertEquals(chatRoom.getTrade(), trade);
	}

	@Test
	@DisplayName("거래 기반 채팅방 생성 테스트")
	void addChatRoomFromSwap() {
		// given
		Long tradeId = 1L;

		Users requester = Users.builder()
			.usersId(1L)
			.build();

		Users owner = Users.builder()
			.usersId(2L)
			.build();

		Goods targetGood = Goods.builder()
			.user(owner)
			.build();

		Goods requestedGood = Goods.builder()
			.user(requester)
			.build();

		Trades trade = new Trades(requestedGood, targetGood);

		ChatRooms chatRoom = new ChatRooms(targetGood, requester, trade);

		ChatRoomAddRequestFromTradeDto dto = new ChatRoomAddRequestFromTradeDto(tradeId);

		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(requester);
		when(chatRoomsRepository.findByTrade(trade)).thenReturn(Optional.empty());
		when(chatRoomsRepository.save(any(ChatRooms.class))).thenReturn(chatRoom);

		// when
		Long chatRoomId = chatService.addChatRoomFromSwap(dto);

		// then
		verify(chatRoomsRepository, times(1)).save(any(ChatRooms.class));
	}

	@Test
	@DisplayName("채팅방 목록 조회 성공")
	void getChatRoomListTest() {
		// given
		Users currentUser = Users.builder()
			.usersId(1L)
			.email("current@example.com")
			.build();

		when(currentUserService.getCurrentUser()).thenReturn(currentUser);

		Users owner = Users.builder()
			.usersId(2L)
			.email("owner@example.com")
			.profileImageUrl("http://example.com/profile.jpg")
			.build();

		ChatRooms chatRoom = ChatRooms.builder()
			.inviter(currentUser)
			.build();

		ReflectionTestUtils.setField(chatRoom, "id", 1L);

		List<ChatRooms> chatRoomsList = List.of(chatRoom);
		when(chatRoomsRepository.findByUsersId(1L)).thenReturn(chatRoomsList);

		when(chatRepository.countByChatRoomsId(any())).thenReturn(1L);

		Chats latestChat = Chats.builder()
			.content("Hello")
			.build();
		when(chatRepository.findTopByChatRoomsIdOrderByCreatedAtDesc(any())).thenReturn(latestChat);

		when(usersRepository.findById(2L)).thenReturn(Optional.of(owner));

		// when
		List<ChatRoomResponseDto> result = chatService.getChatRoomList();

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		ChatRoomResponseDto dto = result.get(0);
		assertEquals(2L, dto.getUsersId());
		assertEquals("http://example.com/profile.jpg", dto.getProfileImageUrl());
		assertEquals("Hello", dto.getRecentChat());
	}

	@Test
	@DisplayName("채팅 목록 조회 성공")
	void getChatList() {
		// given
		Long chatRoomId = 1L;
		Long cursorId = 0L;
		LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

		Users sender1 = Users.builder().usersId(100L).build();
		Users sender2 = Users.builder().usersId(101L).build();

		// 채팅 객체 생성
		// 채팅1: type = REQUEST, goodsId가 필요함.
		Chats chat1 = Chats.builder()
			.chatType(ChatType.REQUEST)
			.content("Request message")
			.goodsId(10L)
			.sender(sender1)
			.build();

		// 채팅2: type = TALK (goodsRepository 호출 없음)
		Chats chat2 = Chats.builder()
			.chatType(ChatType.TALK)
			.content("Talk message")
			.sender(sender2)
			.build();

		List<Chats> chats = List.of(chat1, chat2);
		when(chatRepository.findAllByChatRoomsId(chatRoomId, cursorId, baseTime))
			.thenReturn(chats);

		Users goodsOwner = Users.builder()
			.usersId(200L)
			.nickname("OwnerNick")
			.build();
		Goods goods = Goods.builder()
			.title("Test Good")
			.user(goodsOwner)
			.build();
		when(goodsRepository.findById(10L))
			.thenReturn(Optional.of(goods));

		// when
		ChatListDto result = chatService.getChatList(chatRoomId, cursorId, baseTime);

		// then
		assertEquals(2, result.getChatList().size());

		// 채팅1 (REQUEST): RequesterGoodsDto가 채팅 DTO에 포함되어야 함.
		ChatDto dto1 = result.getChatList().get(0);
		assertEquals(ChatType.REQUEST, dto1.getChatType());
		assertEquals("Request message", dto1.getContent());
		assertNotNull(dto1.getRequesterGoods());
		assertEquals("Test Good", dto1.getRequesterGoods().getTitle());

		// 채팅2 (TALK): RequesterGoodsDto가 null이어야 함.
		ChatDto dto2 = result.getChatList().get(1);
		assertEquals(ChatType.TALK, dto2.getChatType());
		assertEquals("Talk message", dto2.getContent());
		assertNull(dto2.getRequesterGoods());
	}

	@Test
	@DisplayName("채팅 저장 성공")
	void saveChatSuccessTest() {
		// given
		Long chatroomId = 1L;
		Long userId = 100L;
		String email = "test@example.com";

		ChatStompRequestDto chatDto = new ChatStompRequestDto(ChatType.TALK, "Hello, this is a test chat", 123L);

		Users sender = Users.builder().usersId(100L).build();
		Users receiver = Users.builder().usersId(101L).build();

		ChatRooms chatRoom = ChatRooms.builder()
			.id(chatroomId)
			.inviter(sender)
			.build();

		when(chatRoomsRepository.findById(chatroomId)).thenReturn(Optional.of(chatRoom));
		when(usersRepository.findById(userId)).thenReturn(Optional.of(sender));

		Chats savedChat = Chats.builder()
			.chatRooms(chatRoom)
			.sender(sender)
			.chatType(chatDto.getChatType())
			.content(chatDto.getContent())
			.goodsId(chatDto.getGoodsId())
			.build();

		when(chatRepository.save(any(Chats.class))).thenReturn(savedChat);

		// when
		ChatStompResponseDto response = chatService.saveChat(chatroomId, chatDto, userId);

		// then
		assertNotNull(response);
		assertEquals(chatDto.getContent(), response.getContent());
		assertEquals(chatDto.getChatType(), response.getChatType());

		verify(chatRepository, times(1)).save(any(Chats.class));
	}

	@Test
	@DisplayName("채팅방 상품 조회 성공")
	void getChatRoomGoods() {
		// given
		Long chatroomId = 1L;

		Categories category = Categories.builder()
			.name("ELECTRONIC").build();

		Goods goods = Goods.builder()
			.title("Test Good")
			.price(1000L)
			.category(category)
			.placeName("TestPlace")
			.build();

		ChatRooms chatRooms = ChatRooms.builder()
			.id(chatroomId)
			.goods(goods)
			.build();

		when(chatRoomsRepository.findById(chatroomId))
			.thenReturn(Optional.of(chatRooms));

		GoodsImages goodsImages = GoodsImages.builder()
			.s3Key("test-key").build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods))
			.thenReturn(Optional.of(goodsImages));

		String expectedImageUrl = "http://example.com/test-image.jpg";
		when(awsS3Service.generatePreSignedImageUrl("test-key"))
			.thenReturn(expectedImageUrl);

		// when
		ChatRoomGoodsDto result = chatService.getChatRoomGoods(chatroomId);

		// then
		assertNotNull(result);
		assertEquals(goods.getTitle(), result.getTitle());
		assertEquals(goods.getPrice(), result.getPrice());
		assertEquals(category.getName(), result.getCategory());
		assertEquals(expectedImageUrl, result.getImageUrl());
	}
}