package com.example.swapit.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.swapit.repository.CategoryRepository;
import com.example.swapit.repository.GoodRepository;
import com.example.swapit.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class JpaAuditingTest {

	@Autowired
	private GoodRepository goodRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void findGood() {
		// Given
		Users user = userRepository.save(
			Users.builder()
				.nickname("John Doe")
				.profileImageUrl("asdfasdf")
				.email("abc@gmail.com")
				.loginInfo("kakao")
				.build());

		Categories category = categoryRepository.save(Categories.builder()
			.name("Electronics")
			.build());

		Goods goods = Goods.builder()
			.user(user)
			.category(category)
			.title("laptop")
			.price(1000)
			.quality(GoodsQuality.GOOD)
			.content("Brand new laptop for sale.")
			.latitude(37.7749)
			.longitude(-122.4194)
			.placeName("San Francisco")
			.build();

		// When
		Goods savedGoods = goodRepository.save(goods);

		// Then
		assertThat(savedGoods.getCreatedAt()).isNotNull();
		assertThat(savedGoods.getUpdatedAt()).isNotNull();
		assertThat(savedGoods.isDeleted()).isFalse();

		System.out.println(savedGoods.getCreatedAt());
	}

}