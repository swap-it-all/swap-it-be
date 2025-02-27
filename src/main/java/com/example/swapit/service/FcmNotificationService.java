package com.example.swapit.service;

import com.example.swapit.domain.Notifications;

public interface FcmNotificationService {
	void sendFcmNotification(String fcmToken, Notifications noti);
}