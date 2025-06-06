package com.example.swapit.service.chat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class StompSubscriptionService {
	private final ConcurrentHashMap<Long, Set<String>> userSubscriptions = new ConcurrentHashMap<>();

	public void subscribe(Long userId, String destination) {
		userSubscriptions
			.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
			.add(destination);
	}

	public void unsubscribe(Long userId, String destination) {
		Set<String> subs = userSubscriptions.get(userId);
		if (subs != null) {
			subs.remove(destination);
		}
	}

	public boolean isSubscribed(Long userId, String destination) {
		return userSubscriptions
			.getOrDefault(userId, Set.of())
			.contains(destination);
	}

	public void removeUser(Long userId) {
		userSubscriptions.remove(userId);
	}

	public Set<String> getDestinations(Long userId) {
		return userSubscriptions.getOrDefault(userId, Set.of());
	}
}
