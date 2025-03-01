package com.example.swapit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.example.swapit.config.TestConfig;
import com.example.swapit.testcontainer.BaseIntegrationTest;

@SpringBootTest
@Import({TestConfig.class})
class SwapitApplicationTests extends BaseIntegrationTest {

	@Test
	void contextLoads() {
	}

}