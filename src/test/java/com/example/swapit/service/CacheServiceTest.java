package com.example.swapit.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private RedisServiceImpl<String> cacheService;

	private final String testKey = "testKey";
	private final String testValue = "testValue";

	@BeforeEach
	void setUp() {

	}

	@Test
	@DisplayName("key-value 저장 테스트 성공")
	void save_shouldStoreValue() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		cacheService.save(testKey, testValue);
		verify(valueOperations, times(1)).set(testKey, testValue);
	}

	@Test
	@DisplayName("key-value 저장 (만료 시간 포함) 테스트 성공")
	void save_withExpiration_shouldStoreValueWithTTL() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		cacheService.save(testKey, testValue, 60, TimeUnit.SECONDS);
		verify(valueOperations, times(1)).set(testKey, testValue, 60, TimeUnit.SECONDS);
	}

	@Test
	@DisplayName("key 조회 테스트 성공")
	void get_shouldReturnValueIfKeyExists() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(testKey)).thenReturn(testValue);
		String value = cacheService.get(testKey);
		assertThat(value).isEqualTo(testValue);
	}

	@Test
	@DisplayName("key 삭제 테스트 성공")
	void delete_shouldRemoveKey() {
		cacheService.delete(testKey);
		verify(redisTemplate, times(1)).delete(testKey);
	}

	@Test
	@DisplayName("key 존재 확인 테스트 성공")
	void exists_shouldReturnTrueIfKeyExists() {
		when(redisTemplate.hasKey(testKey)).thenReturn(true);
		boolean exists = cacheService.exists(testKey);
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("TTL 확인 테스트 성공")
	void getTTL_shouldReturnRemainingTimeIfKeyExists() {
		when(redisTemplate.getExpire(testKey, TimeUnit.SECONDS)).thenReturn(30L);
		long ttl = cacheService.getTTL(testKey);
		assertThat(ttl).isEqualTo(30);
	}
}