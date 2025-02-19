package com.example.swapit.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.repository.NotificationRepository;

class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private CurrentUserService currentUserService;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	private Users mockUser;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockUser = Users.builder()
			.usersId(1L)
			.nickname("testUser")
			.profileImageUrl("/images/testUser")
			.email("test@gmail.com")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();
	}

	@Test
	@DisplayName("알림 목록 조회 성공")
	void getMyNotifications() {
		// given
		Notifications mockNotification = Notifications.builder()
			.id(1L)
			.user(mockUser)
			.message("Test Notification")
			.url("/test-url")
			.isRead(false)
			.type(NotificationType.REQUESTED)
			.build();

		List<Notifications> mockNotifications = List.of(mockNotification);

		given(currentUserService.getCurrentUser()).willReturn(mockUser);
		given(notificationRepository.findByUserAndReadNotOrderByCreatedAtDesc(mockUser))
			.willReturn(mockNotifications);

		// when
		List<NotificationDto> notifications = notificationService.getMyNotifications();

		// then
		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getMessage()).isEqualTo("Test Notification");
		verify(notificationRepository, times(1)).findByUserAndReadNotOrderByCreatedAtDesc(mockUser);

	}

	@Test
	@DisplayName("알림 읽기 성공")
	void notificationRead_Success() {
		// given
		Notifications mockNotification = Notifications.builder()
			.id(1L)
			.user(mockUser)
			.message("Test Notification")
			.url("/test-url")
			.isRead(false)
			.build();

		given(notificationRepository.findById(anyLong())).willReturn(Optional.of(mockNotification));

		// when
		notificationService.notificationRead(1L);

		// then
		assertThat(mockNotification.isRead()).isTrue();
		verify(notificationRepository, times(1)).save(mockNotification);
	}

	@Test
	@DisplayName("알림 읽기 실패")
	void notificationRead_NotFound() {
		// given
		given(notificationRepository.findById(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> notificationService.notificationRead(1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());

		verify(notificationRepository, times(1)).findById(anyLong());
		verify(notificationRepository, never()).save(any());
	}
}