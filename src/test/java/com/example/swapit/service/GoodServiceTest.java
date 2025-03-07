package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.good.GoodsDetailDto;
import com.example.swapit.domain.dto.good.GoodsRequestDto;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class GoodServiceTest {

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private GoodsImagesRepository goodsImagesRepository;

	@Mock
	private CategoriesRepository categoriesRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private GoodsServiceImpl goodsService;

	private Users testUser;
	private Categories testCategory;
	private Goods testGood;

	@BeforeEach
	void setUp() {
		testUser = Users.builder()
			.usersId(1L)
			.nickname("testUser")
			.profileImageUrl("/images/testUser")
			.email("test@gmail.com")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();

		testCategory = Categories.builder()
			.id(1L)
			.name("ELECTRONICS")
			.build();

		testGood = Goods.builder()
			.user(testUser)
			.title("test 물건")
			.price(1000L)
			.quality(GoodsQuality.NEW)
			.category(testCategory)
			.content("싸게 드려요! 교환주세요!")
			.build();
	}

	@Test
	@DisplayName("상품 상세 정보 조회 테스트")
	void getGoodDetail() {
		// given
		when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGood));
		when(reviewRepository.averageRatingByReviewee(testUser)).thenReturn(4.8);

		// when
		GoodsDetailDto result = goodsService.getGoodDetail(1L);

		// then
		assertNotNull(result);
		assertEquals(testGood.getTitle(), result.getTitle());
		assertEquals(testGood.getPrice(), result.getPrice());
	}

	@Test
	@DisplayName("물건 등록 테스트")
	void insertGood() {
		// given
		GoodsRequestDto requestDto = new GoodsRequestDto(
			"아이폰 14 Pro", 1_300_000L, "NEW", 1L, "용산역 1번 출구", "채팅 주세요!");
		when(currentUserService.getCurrentUser()).thenReturn(testUser);
		when(categoriesRepository.findById(1L)).thenReturn(Optional.of(testCategory));

		// ArgumentCaptor를 생성해서 Goods객체를 캡쳐할 준비
		ArgumentCaptor<Goods> newGoodsCapture = ArgumentCaptor.forClass(Goods.class);

		// when
		goodsService.insertGood(requestDto);

		// then
		verify(goodsRepository, times(1)).save(newGoodsCapture.capture());

		// 캡처된 객체
		Goods newGood = newGoodsCapture.getValue();

		assertEquals(requestDto.getTitle(), newGood.getTitle());
		assertEquals(requestDto.getPrice(), newGood.getPrice());
		assertEquals(testUser, newGood.getUser());
	}

	@Test
	@DisplayName("물건 내용 수정 테스트")
	void updateGood() {
		// given
		GoodsRequestDto requestDto = new GoodsRequestDto("아이폰 14 Pro", 1_300_000L, "NEW", 1L, "용산역 1번 출구", "채팅 주세요!");
		when(currentUserService.getCurrentUser()).thenReturn(testUser);
		when(categoriesRepository.findById(1L)).thenReturn(Optional.of(testCategory));
		when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGood));

		// when
		goodsService.updateGood(1L, requestDto);

		// then
		assertEquals(requestDto.getTitle(), testGood.getTitle());
		assertEquals(requestDto.getPrice(), testGood.getPrice());
		assertEquals(requestDto.getPlaceName(), testGood.getPlaceName());
	}

	@Test
	@DisplayName("물건 삭제 테스트")
	void deleteGood() {
		// given
		when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGood));
		when(currentUserService.getCurrentUser()).thenReturn(testUser);
		when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGood));

		// when
		goodsService.deleteGood(1L);

		// then
		verify(goodsRepository, times(1)).delete(testGood); // delete()가 한 번 호출됐는지 검증
	}
}