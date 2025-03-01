package com.example.swapit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.example.swapit.config.TestFirebaseConfig;
import com.example.swapit.config.TestS3Config;
import com.example.swapit.testcontainer.BaseIntegrationTest;

@SpringBootTest
@Import({TestFirebaseConfig.class, TestS3Config.class})
class SwapitApplicationTests extends BaseIntegrationTest {

	@Test
	void contextLoads() {
	}

}