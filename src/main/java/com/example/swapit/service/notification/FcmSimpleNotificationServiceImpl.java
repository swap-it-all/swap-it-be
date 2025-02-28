package com.example.swapit.service.notification;

import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Notifications;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fcm 알림 메세지 전송 service
 *  : (title-body)로 구성된 알림 전송됨.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSimpleNotificationServiceImpl implements FcmNotificationService {

	private final FirebaseApp firebaseApp;

	@Override
	public void sendFcmNotification(String fcmToken, Notifications noti) {
		FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);

		// todo : 현재는 기본 Notification title-body로 구성된 알림. 이걸 Data Message를 사용해서 내가 원하는 데이터를 보낼 수 있음.
		Message message = Message.builder()
			.setToken(fcmToken)
			.setNotification(Notification.builder()
				.setTitle(noti.getTitle())
				.setBody(noti.getBody())
				.build())
			.build();

		try {
			firebaseMessaging.send(message);
		} catch (Exception e) {
			log.error("[FCM알림LOG 전송실패] Firebase Messaging 실패 : {}", e.getMessage());
			throw new CustomException(ErrorCode.FIREBASE_MESSAGE_ERROR);
		}
	}
}