package com.example.swapit.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class NotificationEvent extends ApplicationEvent {
	private final Long userId;
	private final NotificationType type;
	private final Long relatedData;

	public NotificationEvent(Object source, Long userId, NotificationType type, Long relatedData) {
		super(source);
		this.userId = userId;
		this.type = type;
		this.relatedData = relatedData;
	}

	public Notifications toEntity(Users user) {
		return Notifications.builder()
			.user(user)
			.type(type)
			.title(type.getTitle())
			.body(type.getBody())
			.relatedData(relatedData)
			.isRead(false)
			.build();
	}
}