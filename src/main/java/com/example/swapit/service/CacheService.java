package com.example.swapit.service;

import java.util.concurrent.TimeUnit;

public interface CacheService<T> {
	void save(String key, T value);

	void save(String key, T value, long expirationTime, TimeUnit timeUnit);

	T get(String key);

	void delete(String key);

	boolean exists(String key);

	long getTTL(String key);
}