package com.example.swapit.domain.dto;

import java.time.LocalDateTime;

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
	private final String title;
	private final String body;
	private final String deeplink;
	private final LocalDateTime createdAt;

	public static NotificationDto of(Notifications noti) {
		return NotificationDto.builder()
			.notificationsId(noti.getId())
			.type(noti.getType().toString())
			.deeplink(noti.getDeeplink())
			.title(noti.getTitle())
			.body(noti.getBody())
			.createdAt(noti.getCreatedAt())
			.build();
	}
}