package com.example.swapit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${cloud.aws.redis.host}")
	private String redisHost;

	@Value("${cloud.aws.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory());

		// JSON 직렬화 설정
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

		template.setKeySerializer(new StringRedisSerializer()); // key를 읽기 쉽게 함.
		template.setValueSerializer(serializer); // 객체를 json으로 저장
		template.setHashKeySerializer(new StringRedisSerializer()); // hash 내부 키를 읽기 쉽게 함.
		template.setHashValueSerializer(serializer); // hash 내부 값을 json으로 저장

		return template;
	}
}