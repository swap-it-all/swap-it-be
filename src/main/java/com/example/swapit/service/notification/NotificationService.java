package com.example.swapit.service.notification;

import com.example.swapit.domain.dto.NotificationListDto;

public interface NotificationService {
	NotificationListDto getMyNotifications();

	void notificationRead(Long notificationId);
}