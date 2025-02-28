package com.example.swapit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.swapit.config.TestFirebaseConfig;
import com.example.swapit.testcontainer.BaseIntegrationTest;

@SpringBootTest(classes = TestFirebaseConfig.class)
class SwapitApplicationTests extends BaseIntegrationTest {

	@Test
	void contextLoads() {
	}

}