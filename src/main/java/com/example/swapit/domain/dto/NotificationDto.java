package com.example.swapit.domain.dto;

import com.example.swapit.domain.Notifications;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class NotificationDto {
	private final Long notificationsId;
	private final String type;
	private final String url;
	private final String message;

	public static NotificationDto of(Notifications noti) {
		return NotificationDto.builder()
			.notificationsId(noti.getId())
			.type(noti.getType().toString())
			.url(noti.getUrl())
			.message(noti.getMessage())
			.build();
	}
}