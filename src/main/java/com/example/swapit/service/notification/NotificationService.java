package com.example.swapit.service.notification;

import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.domain.dto.NotificationSettingDto;

public interface NotificationService {
	NotificationListDto getMyNotifications();

	NotificationSettingDto getMyNotificationSetting();

	void notificationRead(Long notificationId);
}