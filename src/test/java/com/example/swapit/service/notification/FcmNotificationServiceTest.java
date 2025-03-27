package com.example.swapit.service.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

@ExtendWith(MockitoExtension.class)
class FcmNotificationServiceTest {

	@Mock
	private FirebaseApp firebaseApp;

	@InjectMocks
	private FcmCustomNotificationServiceImpl fcmService;

	@Test
	@DisplayName("FCM알림 전송 - 성공")
	void testSendFcmNotification_Success() throws FirebaseMessagingException {
		// Given
		String fcmToken = "test_fcm_token";

		Users user = Users.builder().build();
		Notifications noti = new Notifications(1L, user, NotificationType.REQUESTED, "Test Title", "Test Body",
			1L, false);
		ReflectionTestUtils.setField(noti, "createdAt", LocalDateTime.now());

		// FirebaseMessaging을 Mock으로 생성 (mockStatic() 사용)
		try (MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
			FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
			mockedStatic.when(() -> FirebaseMessaging.getInstance(firebaseApp)).thenReturn(firebaseMessaging);

			when(firebaseMessaging.send(any(Message.class))).thenReturn("test_message_id");

			// When
			assertDoesNotThrow(() -> fcmService.sendFcmNotification(fcmToken, noti));

			// Then
			verify(firebaseMessaging, times(1)).send(any(Message.class));
		}
	}
}