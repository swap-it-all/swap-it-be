package com.example.swapit.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

	@Test
	@DisplayName("거래 거절 - 특정 거래를 제외한 나머지 거래들이 REJECTED로 변경되는지 확인")
	void rejectOtherTrades() {
		// Given
		// 거래 생성을 위한 users, goods
		Users requester = Users.builder()
			.nickname("requester")
			.profileImageUrl("requester")
			.email("requester")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
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
		Users owner = Users.builder()
			.nickname("owner")
			.profileImageUrl("owner")
			.email("owner")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
		usersRepository.saveAll(List.of(requester, requester2, requester3, owner));

		Categories category = categoriesRepository.save(
			Categories.builder().name("MISC").build());

		Goods requestGood = Goods.builder()
			.user(requester)
			.title("requestGood")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("requestGood")
			.placeName("place")
			.build();
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
		Goods targetGood = Goods.builder()
			.user(owner)
			.title("targetGood")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.category(category)
			.content("targetGood")
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