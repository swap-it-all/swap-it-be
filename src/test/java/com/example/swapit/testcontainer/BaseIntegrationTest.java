package com.example.swapit.testcontainer;

import org.junit.jupiter.api.BeforeAll;
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

	@BeforeAll
	static void beforeAll() {
		mysqlContainer.start();  // 명시적으로 컨테이너 실행
	}

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}
}