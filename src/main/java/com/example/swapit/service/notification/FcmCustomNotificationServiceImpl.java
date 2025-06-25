package com.example.swapit.service.notification;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.Notifications;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

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
		data.put("notificationsId", noti.getId().toString());
		data.put("type", noti.getType().toString());
		data.put("title", noti.getTitle());
		data.put("body", noti.getBody());
		data.put("relatedData", noti.getRelatedData() != null ? noti.getRelatedData().toString() : "");

		// createdAt은 formatter로 문자형식으로 바꿔서 보냄.
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		data.put("createdAt", noti.getCreatedAt().format(formatter));

		Message message = Message.builder()
			.setToken(fcmToken)
			.setNotification(
				Notification.builder()
					.setTitle(noti.getTitle())
					.setBody(noti.getBody())
					.build())
			.setAndroidConfig(
				AndroidConfig.builder()
					.setPriority(AndroidConfig.Priority.HIGH)
					.setNotification(
						AndroidNotification.builder()
							.setChannelId("swapit_alert_channel")
							.build()
					)
					.build())
			.build();

		try {
			String response = firebaseMessaging.send(message);
			log.debug("[FCM 알림 전송 성공] response : {}", response);
		} catch (Exception e) {
			log.error("[FCM알림 전송실패] Firebase Messaging 실패 : {}", e.getMessage());
		}
	}
}