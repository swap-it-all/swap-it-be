package com.example.swapit.domain.dto;

import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationRequestDto {
	private final Users user;
	private NotificationType type;
	private String message;
	private String url;

	public Notifications toEntity(Users user, NotificationType type) {
		return Notifications.builder()
			.user(user)
			.type(type)
			.message(type.getMessage())
			.url(type.getUrl())
			.isRead(false)
			.build();
	}

	public Notifications toEntity(Users user, NotificationType type, String url) {
		return Notifications.builder()
			.user(user)
			.type(type)
			.message(type.getMessage())
			.url(url)
			.isRead(false)
			.build();
	}
}