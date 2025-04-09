package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.domain.Categories;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.ChatType;
import com.example.swapit.domain.Chats;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
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
	private TradesRepository tradesRepository;

	@Mock
	private NotificationEventPublisher notificationEventPublisher;

	@InjectMocks
	@Spy
	private ChatServiceImpl chatService;

	private final String testCdnUrl = "http://example.com/";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(chatService, "cdnUrl", testCdnUrl);
	}

	@Test
	@DisplayName("물건 기반 채팅방 생성 테스트")
	void addChatRoomFromGood() {
		// Given
		ChatRoomAddRequestFromGoodDto requestDto = new ChatRoomAddRequestFromGoodDto(1L);

		Users requester = Users.builder().usersId(1L).email("test@example.com").build();
		Users owner = Users.builder().usersId(2L).email("owner@example.com").build();

		Goods targetGood = Goods.builder()
			.title("test 물건")
			.price(1000L)
			.quality(GoodsQuality.NEW)
			.content("교환 요청")
			.user(owner)
			.build();

		when(goodsRepository.findById(1L)).thenReturn(Optional.of(targetGood));
		when(currentUserService.getCurrentUser()).thenReturn(requester);

		when(chatRoomsRepository.findByGoodsAndInviter(targetGood, requester))
			.thenReturn(Optional.empty());

		ChatRooms chatRoom = new ChatRooms(targetGood, requester, null);
		ReflectionTestUtils.setField(chatRoom, "id", 100L);

		when(chatRoomsRepository.save(any(ChatRooms.class))).thenReturn(chatRoom);

		// When
		Long chatRoomId = chatService.addChatRoomFromGood(requestDto);

		// Then
		verify(chatRoomsRepository, times(1)).save(any(ChatRooms.class));
		assertEquals(100L, chatRoomId);
	}

	@Test
	@DisplayName("거래 기반 채팅방 생성 테스트")
	void addChatRoomFromSwap() {
		// Given
		Long tradeId = 1L;
		ChatRoomAddRequestFromTradeDto requestDto = new ChatRoomAddRequestFromTradeDto(tradeId);

		Users requester = Users.builder().usersId(1L).build();
		Users owner = Users.builder().usersId(2L).build();

		Goods targetGood = Goods.builder().user(owner).build();
		Goods requestedGood = Goods.builder().user(requester).build();

		Trades trade = new Trades(requestedGood, targetGood);

		when(tradesRepository.findById(tradeId)).thenReturn(Optional.of(trade));
		when(currentUserService.getCurrentUser()).thenReturn(requester);

		when(chatRoomsRepository.findByGoodsAndInviter(targetGood, requester))
			.thenReturn(Optional.empty());

		ChatRooms chatRoom = new ChatRooms(targetGood, requester, trade);
		ReflectionTestUtils.setField(chatRoom, "id", 200L);
		when(chatRoomsRepository.save(any(ChatRooms.class))).thenReturn(chatRoom);

		// When
		Long chatRoomId = chatService.addChatRoomFromSwap(requestDto);

		// Then
		verify(chatRoomsRepository, times(1)).save(any(ChatRooms.class));
		assertEquals(200L, chatRoomId);
	}

	@Test
	@DisplayName("채팅방 목록 조회 성공")
	void getChatRoomListTest() {
		// Given
		Users currentUser = Users.builder()
			.usersId(1L)
			.email("current@example.com")
			.profileImageUrl(testCdnUrl + "profileImage.jpg")
			.build();
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);

		Users owner = Users.builder()
			.usersId(2L)
			.profileImageUrl(testCdnUrl + "profile.jpg")
			.nickname("ownerNick")
			.build();
		Goods goods = Goods.builder()
			.user(owner)
			.build();

		ChatRooms chatRoom = ChatRooms.builder()
			.id(1L)
			.inviter(currentUser)
			.goods(goods)
			.trade(null)
			.inviterLastReadId(3L)
			.notInviterLastReadId(2L)
			.build();

		when(chatRoomsRepository.findMyChatRooms(1L)).thenReturn(List.of(chatRoom));

		when(chatRepository.countByChatRoomsId(1L)).thenReturn(1L);

		Chats latestChat = Chats.builder()
			.content("안녕하세요")
			.build();

		when(chatRepository.findTopByChatRoomsIdOrderByCreatedAtDesc(1L)).thenReturn(latestChat);

		// When
		List<ChatRoomResponseDto> result = chatService.getChatRoomList();

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		ChatRoomResponseDto dto = result.get(0);
		assertEquals(testCdnUrl + "profile.jpg", dto.getProfileImageUrl());
		assertEquals("안녕하세요", dto.getRecentChat());
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

		ChatStompRequestDto chatDto = new ChatStompRequestDto(ChatType.TALK, "Hello, this is a test chat", 123L);

		Users sender = Users.builder().usersId(100L).build();
		Users receiver = Users.builder().usersId(200L).build();
		Goods good = Goods.builder().user(receiver).build();

		ChatRooms chatRoom = ChatRooms.builder()
			.id(chatroomId)
			.inviter(sender)
			.goods(good)
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

		Users currentUser = Users.builder()
			.usersId(2L)
			.nickname("nickname")
			.build();

		Users counterpart = Users.builder()
			.usersId(1L)
			.nickname("닉네임")
			.build();

		Goods goods = Goods.builder()
			.title("Test Good")
			.price(1000L)
			.category(category)
			.placeName("TestPlace")
			.user(currentUser)
			.build();

		Goods targetGoods = Goods.builder()
			.title("Test Targeg Good")
			.user(counterpart)
			.build();

		Trades trades = Trades.builder()
			.id(1L)
			.requestedGoods(goods)
			.targetGoods(targetGoods)
			.status(TradeStatus.PENDING)
			.build();

		ChatRooms chatRooms = ChatRooms.builder()
			.id(chatroomId)
			.goods(goods)
			.trade(trades)
			.build();

		TradeInfoDto trade = new TradeInfoDto(1L, true, "PENDING", 2L);

		when(chatRoomsRepository.findById(chatroomId))
			.thenReturn(Optional.of(chatRooms));

		GoodsImages goodsImages = GoodsImages.builder()
			.s3Key("test-key").build();
		when(goodsImagesRepository.findFirstByGoodOrderByIdAsc(goods))
			.thenReturn(Optional.of(goodsImages));

		String expectedImageUrl = testCdnUrl + goodsImages.getS3Key();

		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(chatService.getCounterpart(chatRooms)).thenReturn(counterpart);
		when(chatService.getTrade(chatRooms.getTrade())).thenReturn(trade);

		// when
		ChatRoomInfoDto result = chatService.getChatRoomInfo(chatroomId);

		// then
		assertNotNull(result);
		assertEquals(goods.getTitle(), result.getTitle());
		assertEquals(goods.getPrice(), result.getPrice());
		assertEquals(category.getName(), result.getCategory());
		assertEquals(expectedImageUrl, result.getImageUrl());
	}

	@Test
	@DisplayName("updateReadReceipt - inviter인 경우 inviterLastReadId 업데이트")
	public void testUpdateReadReceipt_inviter() {
		// given
		Long chatroomId = 1L;
		Long userId = 10L;
		Long lastReadChatId = 100L;

		Users inviter = Users.builder()
			.usersId(userId)
			.build();
		ChatRooms chatRoom = ChatRooms.builder()
			.inviter(inviter)
			.build();

		when(chatRoomsRepository.findById(chatroomId)).thenReturn(Optional.of(chatRoom));

		// when
		chatService.updateReadReceipt(chatroomId, userId, lastReadChatId);

		// then
		assertEquals(lastReadChatId, chatRoom.getInviterLastReadId());
		verify(chatRoomsRepository, times(1)).save(chatRoom);
	}

	@Test
	@DisplayName("updateReadReceipt - 초대자가 아닌 경우 notInviterLastReadId 업데이트")
	public void testUpdateReadReceipt_nonInviter() {
		// given
		Long chatroomId = 1L;
		Long inviterId = 10L;
		Long userId = 20L;
		Long lastReadChatId = 200L;

		Users inviter = Users.builder()
			.usersId(inviterId)
			.build();
		ChatRooms chatRoom = ChatRooms.builder()
			.inviter(inviter)
			.build();

		when(chatRoomsRepository.findById(chatroomId)).thenReturn(Optional.of(chatRoom));

		// when
		chatService.updateReadReceipt(chatroomId, userId, lastReadChatId);

		// then
		assertEquals(lastReadChatId, chatRoom.getNotInviterLastReadId());
		verify(chatRoomsRepository, times(1)).save(chatRoom);
	}
}