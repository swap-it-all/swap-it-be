package com.example.swapit.service.notification;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishNotification(Long userId, NotificationType type, Long relatedData) {
		eventPublisher.publishEvent(new NotificationEvent(this, userId, type, relatedData));
	}
}