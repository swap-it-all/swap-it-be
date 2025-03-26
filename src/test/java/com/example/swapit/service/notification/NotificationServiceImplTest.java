package com.example.swapit.service.notification;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.NotificationType;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.domain.dto.NotificationSettingDto;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Test
	@DisplayName("알림 조회")
	void getMyNotifications() {
		// given
		Users mockUser = new Users();
		Notifications notification = Notifications.builder()
			.id(1L)
			.isRead(false)
			.type(NotificationType.REQUESTED)
			.build();
		List<Notifications> notifications = List.of(notification);

		Mockito.when(currentUserService.getCurrentUser()).thenReturn(mockUser);
		Mockito.when(notificationRepository.findByUserAndReadNotOrderByCreatedAtDesc(mockUser))
			.thenReturn(notifications);

		// when
		NotificationListDto result = notificationService.getMyNotifications();

		// then
		assertThat(result.getNotifications()).hasSize(1);
		assertThat(result.getNotifications().get(0)).isInstanceOf(NotificationDto.class);
	}

	@Test
	@DisplayName("알림 수신 설정 조회 성공")
	void getMyNotificationSetting() {
		// given
		Users user = new Users();
		ReflectionTestUtils.setField(user, "notificationEnabled", true);

		Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);

		// when
		NotificationSettingDto result = notificationService.getMyNotificationSetting();

		// then
		assertThat(result.notificationEnabled()).isTrue();
	}

	@Test
	@DisplayName("알림 읽음 성공")
	void notificationRead() {
		// given
		Long notiId = 1L;
		Notifications notification = Notifications.builder().id(notiId).isRead(false).build();

		Mockito.when(notificationRepository.findById(notiId)).thenReturn(Optional.of(notification));

		// when
		notificationService.notificationRead(notiId);

		// then
		assertThat(notification.isRead()).isTrue();
		Mockito.verify(notificationRepository).save(notification);
	}

	@Test
	@DisplayName("알림읽음 실패 - 알림 없음")
	void notificationRead_fail() {
		// given
		Long notiId = 999L;
		Mockito.when(notificationRepository.findById(notiId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> notificationService.notificationRead(notiId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
	}
}