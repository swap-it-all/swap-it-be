package com.example.swapit.service.notification;

import com.example.swapit.domain.Notifications;

public interface FcmNotificationService {
	void sendFcmNotification(String fcmToken, Notifications noti);
}