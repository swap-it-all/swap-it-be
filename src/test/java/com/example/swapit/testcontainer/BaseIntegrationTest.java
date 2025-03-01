package com.example.swapit.testcontainer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

	@Container
	protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
		.withDatabaseName("test_db")
		.withUsername("root")
		.withPassword("root");

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		waitForDatabaseToBeReady();
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	private static void waitForDatabaseToBeReady() {
		try {
			Thread.sleep(5000); // hibernate가 MySQL 실행을 기다릴 수 있도록 5초 대기
			System.out.println("Waiting for MySQL to be ready...");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}