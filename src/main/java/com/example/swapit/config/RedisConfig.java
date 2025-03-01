package com.example.swapit.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

@Configuration
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory(
		@Value("${spring.data.redis.host}") String redisHost,
		@Value("${spring.data.redis.port}") int redisPort,
		@Value("${spring.data.redis.timeout}") int redisTimeout
	) {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);

		// Lettuce Client 설정 (SSL 및 타임아웃 적용)
		LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
			.commandTimeout(Duration.ofMillis(redisTimeout)) // command 타임아웃 설정
			.clientOptions(ClientOptions.builder()
				.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofSeconds(5)).build()) // 소켓 연결 타임아웃 5초
				.build())
			.useSsl() // TLS 적용
			.build();

		return new LettuceConnectionFactory(config, clientConfig);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(
		@Value("${spring.data.redis.host}") String redisHost,
		@Value("${spring.data.redis.port}") int redisPort,
		@Value("${spring.data.redis.timeout}") int redisTimeout
	) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory(
			redisHost, redisPort, redisTimeout));

		// JSON 직렬화 설정
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

		template.setKeySerializer(new StringRedisSerializer()); // key를 읽기 쉽게 함.
		template.setValueSerializer(serializer); // 객체를 json으로 저장
		template.setHashKeySerializer(new StringRedisSerializer()); // hash 내부 키를 읽기 쉽게 함.
		template.setHashValueSerializer(serializer); // hash 내부 값을 json으로 저장

		return template;
	}
}