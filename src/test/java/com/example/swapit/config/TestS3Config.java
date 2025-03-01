package com.example.swapit.config;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class TestS3Config {

	@Bean
	@Primary
	public S3Client testS3Client() {
		return mock(S3Client.class);
	}
}