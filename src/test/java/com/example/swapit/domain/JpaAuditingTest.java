package com.example.swapit.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.swapit.config.TestFirebaseConfig;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.UsersRepository;
import com.example.swapit.testcontainer.BaseIntegrationTest;

import jakarta.transaction.Transactional;

@SpringBootTest(classes = TestFirebaseConfig.class)
@Transactional
@ActiveProfiles("test")
class JpaAuditingTest extends BaseIntegrationTest {

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