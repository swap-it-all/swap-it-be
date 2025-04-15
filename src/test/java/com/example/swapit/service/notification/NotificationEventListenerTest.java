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
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
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

	@Mock
	private SimpUserRegistry simpUserRegistry;

	@Mock
	private SimpUser simpUser;

	@Mock
	private SimpSession simpSession;

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
			.relatedData(2L)
			.build();
		testEvent = new NotificationEvent(1L, testUser.getUsersId(), NotificationType.CHAT, 2L);
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
		Long userId = 1L;

		Users testUser = Users.builder()
			.usersId(userId)
			.notificationEnabled(true)
			.build();

		when(usersRepository.findById(userId)).thenReturn(Optional.of(testUser));
		when(notificationRepository.save(any(Notifications.class))).thenReturn(mock(Notifications.class));
		when(fcmTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());

		// Simulate user not connected → simpUserRegistry returns null
		when(simpUserRegistry.getUser(String.valueOf(userId))).thenReturn(null);

		// When
		assertDoesNotThrow(() -> {
			notificationEventListener.handleNotification(testEvent);
		});

		// Then
		verify(messagingTemplate, never())
			.convertAndSendToUser(anyString(), anyString(), any());
		verify(fcmNotificationService, never())
			.sendFcmNotification(anyString(), any(Notifications.class));
	}
}