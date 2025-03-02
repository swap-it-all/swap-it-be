package com.example.swapit.service.notification;

import java.util.List;

import com.example.swapit.domain.dto.NotificationDto;

public interface NotificationService {
	List<NotificationDto> getMyNotifications();

	void notificationRead(Long notificationId);
}