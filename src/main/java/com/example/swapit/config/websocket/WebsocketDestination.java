package com.example.swapit.config.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebsocketDestination {
	NOTIFICATION_QUEUE("/user/queue/notifications"),
	CHAT_TOPIC("/topic/chat/");

	private final String path;

	public String withKey(String key) {
		return path + key;
	}
}
