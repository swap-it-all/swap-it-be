package com.example.swapit.service.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;

class NotificationEventPublisherTest {

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private NotificationEventPublisher notificationEventPublisher;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("알림발생 성공")
	void testPublishNotification_WithBasicUrl() {
		// given
		Long userId = 1L;
		NotificationType type = NotificationType.REQUESTED;
		Long relatedData = 2L;

		// when
		notificationEventPublisher.publishNotification(userId, type, relatedData);

		// then
		ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
		verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

		NotificationEvent event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getUserId()).isEqualTo(userId);
		assertThat(event.getType()).isEqualTo(type);
	}
}