package com.example.swapit.service.notification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.Notifications;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class FcmCustomNotificationServiceImpl implements FcmNotificationService {

	private final FirebaseApp firebaseApp;

	@Override
	public void sendFcmNotification(String fcmToken, Notifications noti) {
		FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);

		Map<String, String> data = new HashMap<>();
		data.put("notificationId", noti.getId().toString());
		data.put("type", noti.getType().toString());
		data.put("title", noti.getTitle());
		data.put("body", noti.getBody());
		data.put("deeplink", noti.getDeeplink());

		Message message = Message.builder()
			.setToken(fcmToken)
			.putAllData(data)
			.build();

		try {
			firebaseMessaging.send(message);
		} catch (Exception e) {
			log.error("[FCM알림 전송실패] Firebase Messaging 실패 : {}", e.getMessage());
		}
	}
}