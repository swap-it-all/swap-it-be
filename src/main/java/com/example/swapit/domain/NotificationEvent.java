package com.example.swapit.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class NotificationEvent extends ApplicationEvent {
	private final Long userId;
	private final NotificationType type;
	private final String message;
	private final String url;

	public NotificationEvent(Object source, Long userId, NotificationType type, String message,
		String url) {
		super(source);
		this.userId = userId;
		this.type = type;
		this.message = message;
		this.url = url;
	}

	public Notifications toEntity(Users user) {
		return Notifications.builder()
			.user(user)
			.type(this.type)
			.message(this.message)
			.url(this.url)
			.isRead(false)
			.build();
	}
}