package com.example.swapit.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.service.CacheService;

import lombok.RequiredArgsConstructor;

/**
 * REDIS 테스트용 CONTROLLER
 * todo : ec2와 연동이 되는지 확인 후, 바로 제거 예정.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class RedisTestController {

	private final CacheService<String> cacheService;

	/**
	 * 캐시에 데이터 저장 (기본, 만료 시간 없음)
	 */
	@PostMapping("/set")
	public String set(@RequestParam String key, @RequestParam String value) {
		cacheService.save(key, value);
		return "Key [" + key + "] 저장 완료!";
	}

	/**
	 * 캐시에 데이터 저장 (만료 시간 설정)
	 */
	@PostMapping("/set-expire")
	public String setWithExpire(
		@RequestParam String key,
		@RequestParam String value,
		@RequestParam long expirationTime,
		@RequestParam TimeUnit timeUnit) {
		cacheService.save(key, value, expirationTime, timeUnit);
		return "Key [" + key + "] 저장 완료 (만료시간: " + expirationTime + " " + timeUnit + ")";
	}

	/**
	 * 캐시에서 데이터 조회
	 */
	@GetMapping("/get")
	public String get(@RequestParam String key) {
		String value = cacheService.get(key);
		return (value != null) ? "[RESULT] Key [" + key + "] = " + value : "[ERROR] Key [" + key + "] 존재하지 않음!";
	}

	/**
	 * 캐시에서 데이터 삭제
	 */
	@DeleteMapping("/delete")
	public String delete(@RequestParam String key) {
		cacheService.delete(key);
		return "Key [" + key + "] 삭제 완료!";
	}

	/**
	 * Key 존재 여부 확인
	 */
	@GetMapping("/exists")
	public boolean exists(@RequestParam String key) {
		return cacheService.exists(key);
	}

	/**
	 * TTL(남은 만료 시간) 조회
	 */
	@GetMapping("/ttl")
	public String getTTL(@RequestParam String key) {
		long ttl = cacheService.getTTL(key);
		return (ttl >= 0) ? "[RESULT]  Key [" + key + "] TTL: " + ttl + " 초 남음" : "[ERROR] Key [" + key + "] 존재하지 않음!";
	}
}