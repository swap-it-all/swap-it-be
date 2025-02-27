package com.example.swapit.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class NotificationEvent extends ApplicationEvent {
	private final Long userId;
	private final NotificationType type;
	private final String url;

	public NotificationEvent(Object source, Long userId, NotificationType type) {
		super(source);
		this.userId = userId;
		this.type = type;
		this.url = type.getUrl();
	}

	public NotificationEvent(Object source, Long userId, NotificationType type, Object... urlParams) {
		super(source);
		this.userId = userId;
		this.type = type;
		this.url = type.getUrl(urlParams);
	}

	public Notifications toEntity(Users user) {
		return Notifications.builder()
			.user(user)
			.type(type)
			.title(type.getTitle())
			.body(type.getBody())
			.url(url)
			.isRead(false)
			.build();
	}
}