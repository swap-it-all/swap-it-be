package com.example.swapit.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.swapit.config.TestConfig;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.UsersRepository;
import com.example.swapit.testcontainer.BaseIntegrationTest;

@SpringBootTest
@Import(TestConfig.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaAuditingTest extends BaseIntegrationTest {

	@Container
	private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
		.withDatabaseName("test_db")
		.withUsername("root")
		.withPassword("root");

	// Spring이 TestContainer의 MySQL 정보를 사용하도록 설정
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	private GoodsRepository goodsRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CategoriesRepository categoriesRepository;

	@Test
	void findGood() {
		// Given
		Users user = usersRepository.save(
			Users.builder()
				.nickname("John Doe")
				.profileImageUrl("asdfasdf")
				.email("abc@gmail.com")
				.loginInfo("kakao")
				.role("ROLE_USER")
				.build());

		Categories category = categoriesRepository.save(
			Categories.builder().name("MISC").build());

		Goods goods = Goods.builder()
			.user(user)
			.category(category)
			.title("laptop")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.content("Brand new laptop for sale.")
			.placeName("San Francisco")
			.build();

		// When
		Goods savedGoods = goodsRepository.save(goods);

		// Then
		assertThat(savedGoods.getCreatedAt()).isNotNull();
		assertThat(savedGoods.getUpdatedAt()).isNotNull();
		assertThat(savedGoods.isDeleted()).isFalse();

		System.out.println(savedGoods.getCreatedAt());
	}

}