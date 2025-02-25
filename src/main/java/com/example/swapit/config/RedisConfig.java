package com.example.swapit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${cloud.aws.redis.host}")
	private String redisHost;

	@Value("${cloud.aws.redis.port}")
	private int redisPort;

	private static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory());

		template.setKeySerializer(STRING_SERIALIZER);
		template.setValueSerializer(STRING_SERIALIZER);

		return template;
	}
}