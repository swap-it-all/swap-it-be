package com.example.swapit.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

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
import com.example.swapit.domain.TradeStatus;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dao.ConflictTradeResult;
import com.example.swapit.repository.good.GoodsRepository;
import com.example.swapit.repository.trade.TradesRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
class TradesRepositoryTest {

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
	private TradesRepository tradesRepository;

	@Autowired
	private GoodsRepository goodsRepository;

	@Autowired
	private CategoriesRepository categoriesRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private EntityManager em;

	Users requester, owner;
	Categories category;
	Goods requestGood, targetGood;

	@BeforeEach
	void setUp() {
		requester = Users.builder()
			.nickname("requester")
			.profileImageUrl("requester")
			.email("requester")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
		owner = Users.builder()
			.nickname("owner")
			.profileImageUrl("owner")
			.email("owner")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
		usersRepository.saveAll(List.of(requester, owner));

		category = categoriesRepository.save(
			Categories.builder().name("MISC").build());

		requestGood = Goods.builder()
			.user(requester)
			.title("requestGood")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("requestGood")
			.placeName("place")
			.build();
		targetGood = Goods.builder()
			.user(owner)
			.title("targetGood")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("targetGood")
			.placeName("place")
			.build();
		goodsRepository.saveAll(List.of(requestGood, targetGood));
	}

	@Test
	@DisplayName("거래 요청 - 제약 걸리지 않음.")
	void createTrade_noConflict() {
		// given
		// when
		Optional<ConflictTradeResult> result = tradesRepository.findConflictTrade(requestGood, targetGood, requester);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("거래 요청 실패 - 역방향 요청이 있는 경우")
	void createTrade_conflict0() {
		// given
		tradesRepository.save(new Trades(targetGood, requestGood)); // 이미 반대로 거래가 있음.

		// when
		Optional<ConflictTradeResult> result = tradesRepository.findConflictTrade(requestGood, targetGood, requester);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().type()).isEqualTo(ConflictTradeResult.ConflictType.TRADE_ALREADY_REQUESTED_IN_REVERSE);
	}

	@Test
	@DisplayName("거래 요청 실패 - 같은 거래 조합으로 거래가 이미 존재하는 경우")
	void createTrade_conflict1() {
		// given
		tradesRepository.save(new Trades(requestGood, targetGood));

		// when
		Optional<ConflictTradeResult> result = tradesRepository.findConflictTrade(requestGood, targetGood, requester);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().type()).isEqualTo(
			ConflictTradeResult.ConflictType.TRADE_ALREADY_EXISTS_WITH_SAME_GOODS);
	}

	@Test
	@DisplayName("거래 요청 실패 - 동일한 거래가 과거게 거절된 적이 있는 경우")
	void createTrade_conflict2() {
		// given
		Trades trade = new Trades(requestGood, targetGood);
		trade.setStatus(TradeStatus.REJECTED);
		tradesRepository.save(trade);

		// when
		Optional<ConflictTradeResult> result = tradesRepository.findConflictTrade(requestGood, targetGood, requester);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().type()).isEqualTo(ConflictTradeResult.ConflictType.TRADE_ALREADY_REJECTED);
	}

	@Test
	@DisplayName("거래 요청 실패 - 동일 사용자가 이미 다른 거래로 요청한 경우")
	void createTrade_conflict3() {
		// given
		Goods anotherGood = goodsRepository.save(
			Goods.builder()
				.user(requester)
				.title("requestGood2")
				.price(10000)
				.quality(GoodsQuality.EXCELLENT)
				.category(category)
				.content("requestGood2")
				.placeName("place")
				.build());
		tradesRepository.save(new Trades(anotherGood, targetGood));

		// when
		Optional<ConflictTradeResult> result = tradesRepository.findConflictTrade(requestGood, targetGood, requester);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().type()).isEqualTo(ConflictTradeResult.ConflictType.TRADE_REQUESTED_BY_SAME_USER);
	}

	@Test
	@DisplayName("거래 거절 - 특정 거래를 제외한 나머지 거래들이 REJECTED로 변경되는지 확인")
	void rejectOtherTrades() {
		// Given
		// 거래 생성을 위한 users, goods
		Users requester2 = Users.builder()
			.nickname("requester2")
			.profileImageUrl("requester2")
			.email("requester2")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
		Users requester3 = Users.builder()
			.nickname("requester3")
			.profileImageUrl("requester3")
			.email("requester3")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
		usersRepository.saveAll(List.of(requester2, requester3));

		Goods requestGood2 = Goods.builder()
			.user(requester2)
			.title("requestGood2")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("requestGood2")
			.placeName("place")
			.build();
		Goods requestGood3 = Goods.builder()
			.user(requester3)
			.title("requestGood3")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("requestGood3")
			.placeName("place")
			.build();
		goodsRepository.saveAll(List.of(requestGood, requestGood2, requestGood3, targetGood));

		Trades acceptedTrade = new Trades(requestGood, targetGood);
		Trades pendingTrade1 = new Trades(requestGood2, targetGood);
		Trades pendingTrade2 = new Trades(requestGood3, targetGood);
		tradesRepository.saveAll(List.of(acceptedTrade, pendingTrade1, pendingTrade2));

		// When: 특정 거래를 제외한 나머지를 거절 상태로 변경
		tradesRepository.rejectOtherTrades(targetGood, acceptedTrade.getId());

		em.flush(); // DB에 데이터 반영
		em.clear(); // 영속성 컨텍스트 초기화

		// Then: 상태가 변경되었는지 확인
		List<Trades> tradesList = em.createQuery(
				"SELECT t FROM Trades t WHERE t.targetGoods = :targetGoods AND t.id <> :acceptedTradeId", Trades.class)
			.setParameter("targetGoods", targetGood)
			.setParameter("acceptedTradeId", acceptedTrade.getId())
			.getResultList();

		for (Trades trade : tradesList) {
			assertEquals(TradeStatus.REJECTED, trade.getStatus()); // 나머지 거래는 REJECTED
		}
	}
}