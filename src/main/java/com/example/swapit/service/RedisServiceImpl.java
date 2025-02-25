package com.example.swapit.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl<T> implements CacheService<T> {

	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * Key-Value 저장 (만료 시간 없음)
	 */
	@Override
	public void save(String key, T value) {
		redisTemplate.opsForValue().set(key, value);
	}

	/**
	 * Key-Value 저장 (만료 시간 포함)
	 */
	@Override
	public void save(String key, T value, long expirationTime, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(key, value, expirationTime, timeUnit);
	}

	/**
	 * Key-Value 조회
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T get(String key) {
		return (T)redisTemplate.opsForValue().get(key);
	}

	/**
	 * Key 삭제
	 */
	@Override
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * Key 존재 여부 확인
	 */
	@Override
	public boolean exists(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	/**
	 * TTL(남은 만료 시간) 확인
	 */
	@Override
	public long getTTL(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}
}