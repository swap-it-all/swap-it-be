package com.example.swapit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import com.example.swapit.config.TestConfig;
import com.example.swapit.testcontainer.BaseIntegrationTest;

import jakarta.annotation.PostConstruct;

@SpringBootTest
@Import(TestConfig.class)
class SwapitApplicationTests extends BaseIntegrationTest {

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void printAllBeans() {
		System.out.println("Listing all beans:");
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			System.out.println("➡ Bean: " + beanName);
		}
	}

	@Test
	void contextLoads() {
	}

}