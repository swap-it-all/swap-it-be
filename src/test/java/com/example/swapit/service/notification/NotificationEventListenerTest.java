package com.example.swapit.service.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.repository.FcmTokenRepository;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private FcmTokenRepository fcmTokenRepository;

	@Mock
	private FcmCustomNotificationServiceImpl fcmNotificationService;

	@InjectMocks
	private NotificationEventListener notificationEventListener;

	private Users testUser;
	private Notifications testNotification;
	private NotificationEvent testEvent;

	@BeforeEach
	void setUp() {
		testUser = Users.builder().usersId(1L).build();
		testNotification = Notifications.builder()
			.id(1L)
			.user(testUser)
			.type(NotificationType.CHAT)
			.title(NotificationType.CHAT.getTitle())
			.body(NotificationType.CHAT.getBody())
			.deeplink(NotificationType.CHAT.getDeeplink())
			.build();
		testEvent = new NotificationEvent(1L, testUser.getUsersId(), NotificationType.CHAT);
	}

	@Test
	@DisplayName("유저를 찾을 수 없을 때, 에러 발생.")
	void testHandleNotification_UserNotFound() {
		// Given
		when(usersRepository.findById(1L)).thenReturn(Optional.empty());

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> {
			notificationEventListener.handleNotification(testEvent);
		});

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("FCM 토큰이 없을 때, 에러.")
	void testHandleNotification_FCMTokenNotFound() {
		// Given
		when(usersRepository.findById(1L)).thenReturn(Optional.of(testUser));
		when(notificationRepository.save(any(Notifications.class))).thenReturn(testNotification);
		when(fcmTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());

		// 웹소켓 실패 시 FCM 시도 (그러나 토큰 없음)
		doThrow(new RuntimeException("WebSocket Error"))
			.when(messagingTemplate)
			.convertAndSendToUser(eq("1"), eq("/queue/notifications"), any(NotificationDto.class));

		// When
		assertDoesNotThrow(() -> {
			notificationEventListener.handleNotification(testEvent);
		});

		// Then
		verify(messagingTemplate, times(1))
			.convertAndSendToUser(eq("1"), eq("/queue/notifications"), any(NotificationDto.class));
		verify(fcmNotificationService, never()).sendFcmNotification(anyString(), any(Notifications.class));
	}
}