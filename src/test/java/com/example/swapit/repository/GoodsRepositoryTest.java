package com.example.swapit.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.swapit.config.QueryDslConfig;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GoodsRepositoryTest {

	@Autowired
	private GoodsRepository goodsRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CategoriesRepository categoriesRepository;

	@PersistenceContext
	private EntityManager em;

	private Users testUser;
	private Categories testCategory;
	private Categories testCategory2;

	@BeforeEach
	void setUp() {
		// 테스트용 Users, Categories 저장
		testUser = Users.builder()
			.nickname("testUser")
			.profileImageUrl("/images/testUser")
			.email("test@gmail.com")
			.loginInfo("google")
			.build();
		testCategory = Categories.builder().name("ELECTRONICS").build();
		testCategory2 = Categories.builder().name("APPLIANCES").build();

		usersRepository.save(testUser);
		categoriesRepository.save(testCategory);
		categoriesRepository.save(testCategory2);

		// Goods 저장
		goodsRepository.saveAll(List.of(
			// 전자기기 : ELECTRONICS : 5개
			Goods.builder().user(testUser).title("아이폰 14").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 15").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 16").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 16 Pro").price(10_000L).quality(GoodsQuality.NEW)
				.category(testCategory).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("갤럭시 S25").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory).content("싸게 드려요! 교환주세요!").build(),
			// 가전제품 : APPLIANCES : 3개
			Goods.builder().user(testUser).title("전자레인지").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory2).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("오븐").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory2).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("세탁기").price(1000L).quality(GoodsQuality.NEW)
				.category(testCategory2).content("싸게 드려요! 교환주세요!").build()
		));

		em.flush(); // db 반영
		em.clear(); // 영속성 컨텍스트 초기화
	}

	@Test
	@DisplayName("기본 정렬, 물건 목록 조회")
	void findGoods() {
		// given
		// 마지막 커서 id 없음
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, null, null, null, size
		);

		// then
		assertEquals(size + 1, results.size());

		for (Goods good : results) {
			System.out.println("Goods ID: " + good.getId() + "  | Title: " + good.getTitle());
		}
	}

	@Test
	@DisplayName("마지막 커서 기반, 최신순 물건 목록 조회")
	void findGoodsByCursor_sortByRecent() {
		// given
		Goods cursorGoods = goodsRepository.findAll()
			.stream()
			.max(Comparator.comparing(Goods::getCreatedAt)) // 최신 데이터 찾기
			.orElseThrow();
		Long cursor = cursorGoods.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		// cursor를 createdAt 값으로 설정

		List<Long> categoryIds = List.of(testCategory.getId());
		String sortBy = "recent";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			cursor, categoryIds, null, sortBy, size
		);

		// then
		for (Goods good : results) {
			System.out.println("Good Title: " + good.getTitle() + " | Good Category ID: " + good.getCategory().getId());
		}

		assertEquals(size + 1, results.size());
		for (Goods good : results) {
			assertTrue(good.getCreatedAt().isBefore(cursorGoods.getCreatedAt()),
				"Good ID: " + good.getId() + " 의 createdAt이 커서보다 오래된 데이터여야 합니다.");
		}
	}

	@Test
	@DisplayName("마지막 커서 기반, 가격 높은 순 물건 목록 조회")
	void findGoodsByCursor_sortByPriceHigh() {
		// given
		Goods cursorGoods = goodsRepository.findAll()
			.stream()
			.max(Comparator.comparing(Goods::getPrice)) // 가장 높은 가격 데이터 찾기.
			.orElseThrow();
		Long cursor = cursorGoods.getPrice();

		List<Long> categoryIds = List.of(testCategory.getId());
		String sortBy = "priceHigh";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			cursor, categoryIds, null, sortBy, size
		);

		// then
		for (Goods good : results) {
			System.out.println("Good Title: " + good.getTitle() + " | Good Category ID: " + good.getCategory().getId());
		}

		assertEquals(size + 1, results.size());
		for (Goods good : results) {
			assertTrue(good.getPrice() < cursor,
				"Good ID: " + good.getId() + " 의 가격이 cursor보다 낮은 가격이여야 합니다.");
		}
	}

	@Test
	@DisplayName("특정 카테고리로 필터링된 물건 목록")
	void findBoodsByCategory() {
		// given
		List<Long> categoryIds = List.of(testCategory2.getId()); // 가전제품
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, categoryIds, null, null, size
		);

		// then
		for (Goods good : results) {
			System.out.println("Good Title: " + good.getTitle() + " | Good Category ID: " + good.getCategory().getId());
		}
		assertFalse(results.isEmpty(), "조회 된 목록이 비어있지 않아야 합니다.");
		assertTrue(results.stream().allMatch(c -> c.getCategory().getId().equals(testCategory2.getId())),
			"모든 결과값의 카테고리가 " + testCategory2.getId() + " 이(가) 아닙니다.");
	}

	@Test
	@DisplayName("최신순으로 정렬된 물건 목록 조회")
	void findGoods_sortedByRecent() {
		// given
		// 마지막 커서 id 없음
		List<Long> categoryIds = List.of(testCategory2.getId());
		String sortBy = "recent";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, categoryIds, null, sortBy, size
		);

		// then
		assertEquals(size + 1, results.size());

		// createdAt이 내림차순인지 확인
		for (int i = 0; i < results.size() - 1; i++) {
			assertTrue(
				results.get(i).getCreatedAt().isAfter(results.get(i + 1).getCreatedAt()) ||
					results.get(i).getCreatedAt().isEqual(results.get(i + 1).getCreatedAt())
			);
		}
	}

	@Test
	@DisplayName("가격 높은 순으로 정렬된 물건 목록 조회")
	void findGoods_sortedByHighPrice() {
		// given
		List<Long> categoryIds = List.of(testCategory.getId());
		String sortBy = "priceHigh";
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, categoryIds, null, sortBy, size
		);

		// then
		assertEquals(size + 1, results.size()); // ✅ 개수 검증

		// ✅ 가격 높은 순 검증
		for (int i = 0; i < results.size() - 1; i++) {
			assertTrue(results.get(i).getPrice() >= results.get(i + 1).getPrice());
		}
	}

	@Test
	@DisplayName("검색어 필터링이 적용된 물건 목록 조회")
	void findGoodsByKeyword() {
		// given
		List<Long> categoryIds = List.of(testCategory.getId());
		String keyword = "갤럭시";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, categoryIds, keyword, null, size
		);

		// then
		assertFalse(results.isEmpty()); // 갤럭시 매물이 잇어야 함.
		assertTrue(results.stream().allMatch(t -> t.getTitle().contains(keyword))); // 키워드 포함 확인.
	}
}