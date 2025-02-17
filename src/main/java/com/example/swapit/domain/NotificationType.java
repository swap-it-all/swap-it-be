package com.example.swapit.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationType {
	REQUESTED("스왑 요청"),
	ACCEPTED("스왑 수락"),
	REJECTED("스왑 거절"),
	COMPLETED("스왑 완료"),
	REVIEW("리뷰 도착"),
	CHAT("채팅");

	private final String description;
}