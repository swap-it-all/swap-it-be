package com.example.swapit.config;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.firebase.FirebaseApp;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@TestConfiguration
public class TestConfig {

	@Bean
	@Primary
	public FirebaseApp firebaseAppMock() {
		return mock(FirebaseApp.class);
	}

	@Bean
	@Primary
	public S3Client testS3Client() {
		return mock(S3Client.class);
	}

	@Bean
	@Primary
	public S3Presigner testS3Presigner() {
		return mock(S3Presigner.class);
	}

	@Bean
	@Primary
	public RedisConnectionFactory mockRedisConnectionFactory() {
		return mock(RedisConnectionFactory.class);
	}

	@Bean
	@Primary
	public RedisTemplate<String, Object> mockRedisTemplate() {
		return mock(RedisTemplate.class);
	}
}