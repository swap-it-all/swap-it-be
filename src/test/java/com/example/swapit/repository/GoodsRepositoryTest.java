package com.example.swapit.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@ActiveProfiles("test")
@Testcontainers
class GoodsRepositoryTest {

	@Container
	private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
		.withDatabaseName("test_db")
		.withUsername("root")
		.withPassword("root");

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	private GoodsRepository goodsRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CategoriesRepository categoriesRepository;

	@PersistenceContext
	private EntityManager em;

	private Users testUser;
	private Categories category;
	private Categories category2;

	@BeforeEach
	void setUp() {
		// 테스트용 Users, Categories 저장
		testUser = Users.builder()
			.nickname("testUser")
			.profileImageUrl("/images/testUser")
			.email("test@gmail.com")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();

		category = categoriesRepository.save(
			Categories.builder().name("MISC").build());

		category2 = categoriesRepository.save(
			Categories.builder().name("MISC").build());

		usersRepository.save(testUser);
		categoriesRepository.save(category);
		categoriesRepository.save(category2);

		// Goods 저장
		goodsRepository.saveAll(List.of(
			// 전자기기 : ELECTRONICS : 5개
			Goods.builder().user(testUser).title("아이폰 14").price(1000L).quality(GoodsQuality.NEW)
				.category(category).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 15").price(1000L).quality(GoodsQuality.NEW)
				.category(category).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 16").price(1000L).quality(GoodsQuality.NEW)
				.category(category).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("아이폰 16 Pro").price(10_000L).quality(GoodsQuality.NEW)
				.category(category).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("갤럭시 S25").price(1000L).quality(GoodsQuality.NEW)
				.category(category).content("싸게 드려요! 교환주세요!").build(),
			// 가전제품 : APPLIANCES : 3개
			Goods.builder().user(testUser).title("전자레인지").price(1000L).quality(GoodsQuality.NEW)
				.category(category2).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("오븐").price(1000L).quality(GoodsQuality.NEW)
				.category(category2).content("싸게 드려요! 교환주세요!").build(),
			Goods.builder().user(testUser).title("세탁기").price(1000L).quality(GoodsQuality.NEW)
				.category(category2).content("싸게 드려요! 교환주세요!").build()
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
			null, null, null, null, null, null, size
		);

		// then
		assertEquals(size + 1, results.size());
	}

	@Test
	@DisplayName("최신순으로 정렬된 물건 목록 조회")
	void findGoods_sortedByRecent() {
		// given
		List<Long> categoryIds = List.of(category.getId());
		String sortBy = "recent";
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(null, null, null, categoryIds, null, sortBy, size);

		// then
		assertEquals(size + 1, results.size());

		for (int i = 0; i < results.size() - 1; i++) { // createdAt이 내림차순인지 확인
			assertTrue(results.get(i).getCreatedAt().isAfter(results.get(i + 1).getCreatedAt()) ||
				results.get(i).getCreatedAt().isEqual(results.get(i + 1).getCreatedAt()));
		}
	}

	@Test
	@DisplayName("가격 높은 순으로 정렬된 물건 목록 조회")
	void findGoods_sortedByHighPrice() {
		// given
		List<Long> categoryIds = List.of(category.getId());
		String sortBy = "priceHigh";
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(null, null, null, categoryIds, null, sortBy, size);

		// then
		assertEquals(size + 1, results.size());

		for (int i = 0; i < results.size() - 1; i++) { // 가격 높은 순 검증
			assertTrue(results.get(i).getPrice() >= results.get(i + 1).getPrice());
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

		LocalDateTime cursorCreatedAt = cursorGoods.getCreatedAt();
		Long cursorId = cursorGoods.getId(); // 최신 데이터의 goodsId를 추가

		List<Long> categoryIds = List.of(category.getId());
		String sortBy = "recent";
		int size = 2; // 조회할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, cursorId, cursorCreatedAt, categoryIds, null, sortBy, size
		);

		// then
		assertEquals(size + 1, results.size());
		for (Goods good : results) {
			assertTrue(
				good.getCreatedAt().isBefore(cursorGoods.getCreatedAt()) ||
					(good.getCreatedAt().isEqual(cursorGoods.getCreatedAt()) && good.getId() > cursorId),
				"Good ID: " + good.getId() + " 의 createdAt이 커서보다 오래된 데이터여야 하며, 같은 시간이라면 goodsId가 커야 합니다."
			);
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

		Long cursorValue = cursorGoods.getPrice(); // 가격을 기준으로
		Long cursorId = cursorGoods.getId(); // 상품 ID 추가

		List<Long> categoryIds = List.of(category.getId());
		String sortBy = "priceHigh";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			cursorValue, cursorId, null, categoryIds, null, sortBy, size
		);

		// then
		assertEquals(size + 1, results.size());
		for (Goods good : results) {
			assertTrue(good.getPrice() < cursorValue ||
					(good.getPrice() == cursorValue && good.getId() > cursorId),
				"Good ID: " + good.getId() + " 의 가격이 cursor보다 낮거나, 같은 가격이면 goodsId가 커야 합니다."
			);
		}
	}

	@Test
	@DisplayName("특정 카테고리로 필터링된 물건 목록")
	void findBoodsByCategory() {
		// given
		List<Long> categoryIds = List.of(category2.getId()); // 가전제품
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, null, null, categoryIds, null, null, size
		);

		// then
		assertFalse(results.isEmpty(), "조회 된 목록이 비어있지 않아야 합니다.");
		assertTrue(results.stream().allMatch(c -> c.getCategory().getId().equals(category2.getId())),
			"모든 결과값의 카테고리가 " + category2.getId() + " 이(가) 아닙니다.");
	}

	@Test
	@DisplayName("검색어 필터링이 적용된 물건 목록 조회")
	void findGoodsByKeyword() {
		// given
		List<Long> categoryIds = List.of(category.getId());
		String keyword = "갤럭시";
		int size = 2; // 조회 할 데이터 개수

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(
			null, null, null, categoryIds, keyword, null, size
		);

		// then
		assertFalse(results.isEmpty()); // 갤럭시 매물이 잇어야 함.
		assertTrue(results.stream().allMatch(t -> t.getTitle().contains(keyword))); // 키워드 포함 확인.
	}

	@Test
	@DisplayName("가격이 같은 여러 물건 중 1번째 물건을 cursorId로 다음 데이터를 조회")
	void findGoodsByCursor_samePrice() {
		// given
		// 가격이 1000원인 상품 중 첫 번째 상품 찾기
		Goods firstGoods = goodsRepository.findAll().stream()
			.filter(g -> g.getPrice() == 1000L)
			.min(Comparator.comparing(Goods::getId))
			.orElseThrow();

		Long cursorPrice = firstGoods.getPrice();
		Long cursorId = firstGoods.getId();
		String sortBy = "priceHigh";
		int size = 2;

		// when
		List<Goods> results = goodsRepository.findGoodsByCursor(cursorPrice, cursorId, null, null, null, sortBy, size);

		// then
		assertFalse(results.isEmpty());
		assertEquals(size + 1, results.size());

		for (Goods good : results) {
			assertTrue(good.getPrice() <= cursorPrice);
			if (good.getPrice() == cursorPrice) {
				assertTrue(good.getId() > cursorId);
			}
		}
	}
}